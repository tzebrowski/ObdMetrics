 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.executor;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.api.CommandProducer;
import org.obd.metrics.command.dtc.DtcFollowUpResponse;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.api.model.SnapshotPID;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.VinPids;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeSnapshotCodec;
import org.obd.metrics.command.dtc.DtcModuleStatus;
import org.obd.metrics.command.dtc.DtcProgress;
import org.obd.metrics.command.dtc.DtcReadReply;
import org.obd.metrics.command.dtc.UdsSnapshotResponse;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeProgressCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeScheduleCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeStartCommand;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class DiagnosticTroubleCodeHandler extends ReplyObserver<ObdMetric> implements CommandHandler {

	private static final int MAX_WAIT_TIME_MS = 2500;
	// Per follow-up request on top of the base wait: dozens of codes x requests do not fit in a fixed 5s.
	private static final int PER_REQUEST_WAIT_TIME_MS = 300;
	private static final int SNAPSHOT_PID_BASE_ID = 999999;
	private static final String DTC_PLACEHOLDER = "{dtc}";

	private final Set<DiagnosticTroubleCode> dtcAccumulator = ConcurrentHashMap.newKeySet();
	// Keyed by module + DTC hex (see key()): two modules can report the same code.
	private final Map<String, UdsSnapshotResponse> snapshots = new ConcurrentHashMap<>();
	private final Map<String, List<SnapshotPID>> details = new ConcurrentHashMap<>();
	// Latest clear reply per module ("" when untagged). Keyed by module so a stale reply - e.g.
	// from the automatic clear during init, which this observer also sees - cannot stand in for a
	// module the scheduled clear targeted.
	private final Map<String, DiagnosticTroubleCodeClearStatus> clearStatuses = new ConcurrentHashMap<>();
	// How each module answered the read of the action in flight ("" when untagged). A module with no reply at all
	// (NO DATA, timeout) never reaches onNext - ObdCommandHandler drops adapter errors - so it stays absent here and
	// is reported as NO_RESPONSE.
	private final Map<String, DtcModuleStatus> moduleStatuses = new ConcurrentHashMap<>();

	private volatile CountDownLatch snapshotLatch;

	// The VIN read at the start of the action (ExecutionContext): null until a VIN request answers, "" for an ECU
	// without a programmed VIN.
	private volatile String vin;
	
	private final CommandsBuffer commandBuffer;
	private final CommandProducer commandProducer;
	private final PidDefinitionRegistry pidRegistry;
	private final Subscription subscription;
	
	DiagnosticTroubleCodeHandler(CommandsBuffer commandsBuffer, CommandProducer commandProducer,
			PidDefinitionRegistry pidRegistry, EventsPublishlisher eventsPublishlisher, Subscription subscription) {

		this.commandBuffer = commandsBuffer;
		this.commandProducer = commandProducer;
		this.pidRegistry = pidRegistry;
		this.subscription = subscription;
		eventsPublishlisher.subscribe(this);
	}

	@Override
	public void onNext(ObdMetric reply) {

		final PIDsGroup group = reply.getCommand().getPid().getGroup();
		if (group == PIDsGroup.DTC_READ || group == PIDsGroup.DTC_OBD_READ) {
			accumulate((List<DiagnosticTroubleCode>) reply.getValue());
			// Any decoded reply means the module answered; the codec marks a refusal. The OBD-II header gets three
			// An OBD-II module gets three reads (03 / 07 / 0A): one answered read is enough, an ECU without service 0A
			// is not "refused".
			final DtcModuleStatus status = reply.getValue() instanceof DtcReadReply
					? ((DtcReadReply) reply.getValue()).getStatus()
					: DtcModuleStatus.RESPONDED;
			moduleStatuses.merge(Objects.toString(reply.getCommand().getPid().getModule(), ""), status,
					(previous, next) -> previous == DtcModuleStatus.RESPONDED ? previous : next);
		}

		if (group == PIDsGroup.METADATA && VinPids.isVin(reply.getCommand().getPid()) && reply.getValue() != null) {
			vin = VinPids.preferred(vin, reply.getValue().toString());
		}

		if ((group == PIDsGroup.DTC_CLEAR || group == PIDsGroup.DTC_OBD_CLEAR)
				&& reply.getValue() instanceof DiagnosticTroubleCodeClearStatus) {
			clearStatuses.put(Objects.toString(reply.getCommand().getPid().getModule(), ""),
					(DiagnosticTroubleCodeClearStatus) reply.getValue());
		}

		if (reply.getValue() instanceof UdsSnapshotResponse) {
			final UdsSnapshotResponse value = (UdsSnapshotResponse) reply.getValue();
			snapshots.put(key(reply.getCommand().getPid().getModule(), value.getDtcHex()), value);

			if (snapshotLatch != null) {
				snapshotLatch.countDown();
			}
		}

		if (reply.getValue() instanceof DtcFollowUpResponse) {
			final DtcFollowUpResponse value = (DtcFollowUpResponse) reply.getValue();
			details.computeIfAbsent(key(reply.getCommand().getPid().getModule(), value.getDtcHex()),
					k -> new CopyOnWriteArrayList<>()).addAll(value.getValues());

			if (snapshotLatch != null) {
				snapshotLatch.countDown();
			}
		}
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		if (command instanceof DiagnosticTroubleCodeStartCommand) {
			// Anything accumulated so far (the init read, a timed-out earlier action) is stale.
			log.info("DTC action started. Dropping {} stale DTCs and {} clear statuses.", dtcAccumulator.size(),
					clearStatuses.size());
			dtcAccumulator.clear();
			clearStatuses.clear();
			moduleStatuses.clear();
			snapshots.clear();
			details.clear();
			vin = null;
			return CommandExecutionStatus.OK;
		}

		if (command instanceof DiagnosticTroubleCodeProgressCommand) {
			final DtcProgress progress = ((DiagnosticTroubleCodeProgressCommand) command).getProgress();
			log.info("DTC progress: {}", progress);
			subscription.onDTCProgress(progress);
			return CommandExecutionStatus.OK;
		}

		final DiagnosticTroubleCodeScheduleCommand scheduleCommand = (DiagnosticTroubleCodeScheduleCommand) command;
		log.info("Executing DiagnosticTroubleCodeHandler for actions: {}", scheduleCommand.getActions());

		CompletableFuture.runAsync(() -> {
			try {
				long time = System.currentTimeMillis();

				final Set<DiagnosticTroubleCode> dtcValue = drainAccumulator();
				// Clear replies precede the reads, so they are in by the time the reads are drained.
				final DiagnosticTroubleCodeClearStatus clearStatus = drainClearStatus(scheduleCommand);
				final Map<String, DtcModuleStatus> moduleStatus = drainModuleStatus(scheduleCommand);

				if (dtcValue.isEmpty()) {
					log.warn("DTC polling timed out or no Diagnostic Trouble Codes found.");
					notifySubscribers(Collections.emptySet(), clearStatus, moduleStatus);
					return;
				}

				log.info("Found Diagnostic Trouble Codes, length: {}.", dtcValue.size());

				if (scheduleCommand.getActions().contains(DtcAction.READ_SNAPSHPOTS)) {
					processFollowUps(dtcValue, scheduleCommand);
				}

				notifySubscribers(dtcValue, clearStatus, moduleStatus);
				time = System.currentTimeMillis() - time;

				log.info("DTC handler was procssing in {}ms", time);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("DTC execution thread was interrupted", e);
			} catch (Exception e) {
				log.error("An unexpected error occurred during DTC execution", e);
			}
		});

		return CommandExecutionStatus.OK;
	}

	// One code reported by several OBD-II services (stored in 03, permanent in 0A) is one code with the states of all.
	// DiagnosticTroubleCode equality ignores the states, so a plain addAll kept the first and dropped the rest.
	private void accumulate(final List<DiagnosticTroubleCode> codes) {
		synchronized (dtcAccumulator) {
			for (final DiagnosticTroubleCode dtc : codes) {
				final DiagnosticTroubleCode existing = dtcAccumulator.stream().filter(dtc::equals).findFirst().orElse(null);
				if (existing == null) {
					dtcAccumulator.add(dtc);
				} else {
					existing.setStatusMask(existing.getStatusMask() | dtc.getStatusMask());
					existing.setPermanent(existing.isPermanent() || dtc.isPermanent());
					final Set<String> statuses = new LinkedHashSet<>();
					if (existing.getActiveStatuses() != null) {
						statuses.addAll(existing.getActiveStatuses());
					}
					if (dtc.getActiveStatuses() != null) {
						statuses.addAll(dtc.getActiveStatuses());
					}
					existing.setActiveStatuses(new ArrayList<>(statuses));
				}
			}
		}
	}

	// Commands are dispatched to the adapter strictly one at a time, so every DTC read command
	// scheduled ahead of this schedule-marker command has already completed and populated the
	// accumulator by the time this runs. The bounded wait only guards against residual async lag.
	private Set<DiagnosticTroubleCode> drainAccumulator() throws InterruptedException {
		final long deadline = System.currentTimeMillis() + MAX_WAIT_TIME_MS;
		while (dtcAccumulator.isEmpty() && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
		}

		final Set<DiagnosticTroubleCode> dtcValue = new HashSet<>(dtcAccumulator);
		dtcAccumulator.clear();
		return dtcValue;
	}

	// Every targeted module's status, in scan order; a module without a read reply is NO_RESPONSE. Empty for a
	// default-header read (no modules).
	private Map<String, DtcModuleStatus> drainModuleStatus(final DiagnosticTroubleCodeScheduleCommand command) {
		try {
			final Map<String, DtcModuleStatus> result = new LinkedHashMap<>();
			if (command.getModules() != null) {
				command.getModules().forEach(module -> result.put(module,
						moduleStatuses.getOrDefault(Objects.toString(module, ""), DtcModuleStatus.NO_RESPONSE)));
			}
			log.info("DTC read status per module: {}", result);
			return result;
		} finally {
			moduleStatuses.clear();
		}
	}

	// The outcome of the scheduled clear across its modules: ERR if any module refused, OK if the
	// others acknowledged it, NO_DATA when no clear was scheduled or no targeted module answered.
	private DiagnosticTroubleCodeClearStatus drainClearStatus(final DiagnosticTroubleCodeScheduleCommand command) {
		try {
			if (!command.getActions().contains(DtcAction.CLEAR)) {
				return DiagnosticTroubleCodeClearStatus.NO_DATA;
			}

			final List<String> modules = command.getModules();
			final List<DiagnosticTroubleCodeClearStatus> statuses = new ArrayList<>();
			if (modules == null || modules.isEmpty()) {
				statuses.addAll(clearStatuses.values());
			} else {
				modules.forEach(module -> {
					final DiagnosticTroubleCodeClearStatus status = clearStatuses.get(Objects.toString(module, ""));
					if (status != null) {
						statuses.add(status);
					}
				});
			}

			log.info("DTC clear statuses for modules {}: {}", modules, statuses);
			if (statuses.contains(DiagnosticTroubleCodeClearStatus.ERR)) {
				return DiagnosticTroubleCodeClearStatus.ERR;
			}
			return statuses.contains(DiagnosticTroubleCodeClearStatus.OK) ? DiagnosticTroubleCodeClearStatus.OK
					: DiagnosticTroubleCodeClearStatus.NO_DATA;
		} finally {
			clearStatuses.clear();
		}
	}

	// Context reads per code: the UDS snapshot (19 04) plus every dtcFollowUp template of the loaded PID files.
	// Each request goes to the module the code was read from - after a multi-module scan the adapter's header
	// still points at the last module, so without the ATSH every request went to that one ECU.
	private void processFollowUps(final Set<DiagnosticTroubleCode> dtcValue,
			final DiagnosticTroubleCodeScheduleCommand scheduleCommand) throws InterruptedException {

		long processingTime = System.currentTimeMillis();
		final Collection<PidDefinition> templates = pidRegistry.findBy(PIDsGroup.DTC_FOLLOW_UP);
		log.info("Reading DTC snapshots and {} follow-up request(s) per code.", templates.size());

		final Map<String, String> headers = new HashMap<>();
		final Set<String> obdModules = new HashSet<>();
		if (scheduleCommand.getHeaders() != null) {
			scheduleCommand.getHeaders().forEach(h -> {
				headers.put(h.getMode(), h.getHeader());
				if (h.getDtcProtocol() == Init.DtcProtocol.OBD) {
					obdModules.add(h.getMode());
				}
			});
		}

		// UDS requests only: codes of an OBD-II module have no UDS module to ask.
		final Map<String, List<DiagnosticTroubleCode>> byModule = new LinkedHashMap<>();
		dtcValue.stream().filter(dtc -> !obdModules.contains(dtc.getModule()))
				.forEach(dtc -> byModule.computeIfAbsent(Objects.toString(dtc.getModule(), ""), k -> new ArrayList<>())
						.add(dtc));

		final int udsCodes = byModule.values().stream().mapToInt(List::size).sum();
		if (udsCodes == 0) {
			log.info("No codes to read follow-ups for.");
			return;
		}

		final int requests = udsCodes * (1 + templates.size());
		subscription.onDTCProgress(DtcProgress.followUps(udsCodes));
		snapshotLatch = new CountDownLatch(requests);

		commandBuffer.clear();
		commandProducer.pause();

		byModule.forEach((module, codes) -> {
			final String header = headers.get(module);
			if (header != null && header.length() > 0) {
				commandBuffer.addLast(new ATCommand("SH" + header));
			}

			for (final DiagnosticTroubleCode dtc : codes) {
				if (log.isDebugEnabled()) {
					log.debug("Adding DTC follow-up requests for '{}' (module '{}') to the buffer", dtc.getRawHex(), module);
				}

				addFollowUp(new PidDefinition(SNAPSHOT_PID_BASE_ID, "1904",
						String.format("19 04 %s FF", dtc.getRawHex()), "DTC Read Snapshot", "19",
						DiagnosticTroubleCodeSnapshotCodec.class.getName()), dtc);

				for (final PidDefinition template : templates) {
					final String pid = template.getPid().replace(DTC_PLACEHOLDER, dtc.getRawHex());
					addFollowUp(new PidDefinition(template.getId(), pid, template.getMode() + pid,
							template.getDescription(), template.getMode(), template.getCodecClass()), dtc);
				}
			}
		});

		commandProducer.resetHeader();
		commandProducer.resume();

		final long timeout = MAX_WAIT_TIME_MS * 2 + (long) requests * PER_REQUEST_WAIT_TIME_MS;
		if (!snapshotLatch.await(timeout, TimeUnit.MILLISECONDS)) {
			log.warn("Timed out waiting for DTC follow-ups. Expected: {}, still missing: {}", requests,
					snapshotLatch.getCount());
		}

		dtcValue.forEach(dtc -> {
			final String key = key(dtc.getModule(), dtc.getRawHex());
			final UdsSnapshotResponse udsSnapshotResponse = snapshots.get(key);
			if (udsSnapshotResponse != null) {
				dtc.setSnapshot(udsSnapshotResponse.getExtractedDids());
			}
			final List<SnapshotPID> values = details.get(key);
			if (values != null) {
				dtc.setDetails(new ArrayList<>(values));
			}
		});

		processingTime = System.currentTimeMillis() - processingTime;
		log.info("DTC follow-ups were processed in {}ms", processingTime);
	}

	// Registered once per query: a second registration under the same query would make the decoder publish the
	// reply twice (once per registered PID), counting the latch down twice. The command carries the module.
	private void addFollowUp(final PidDefinition pid, final DiagnosticTroubleCode dtc) {
		if (pidRegistry.findAllBy(pid).isEmpty()) {
			pidRegistry.register(pid);
		}
		commandBuffer.addLast(new ObdCommand(pid.withModule(dtc.getModule())));
	}

	private static String key(final String module, final String dtcHex) {
		return Objects.toString(module, "") + "|" + Objects.toString(dtcHex, "").toUpperCase();
	}

	private void notifySubscribers(Set<DiagnosticTroubleCode> dtcs, DiagnosticTroubleCodeClearStatus clearStatus,
			Map<String, DtcModuleStatus> moduleStatus) {

		log.info ("Notyfing about {} DTCs found, clear status: {}", dtcs.size(), clearStatus);
		subscription.onDTCCompleted(dtcs, clearStatus, moduleStatus, vin);
		snapshots.clear();
		details.clear();
	}
}