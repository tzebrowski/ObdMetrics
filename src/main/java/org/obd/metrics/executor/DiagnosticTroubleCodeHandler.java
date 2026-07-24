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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.api.CommandProducer;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeSnapshotCodec;
import org.obd.metrics.command.dtc.UdsSnapshotResponse;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeScheduleCommand;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class DiagnosticTroubleCodeHandler extends ReplyObserver<ObdMetric> implements CommandHandler {

	private static final int MAX_WAIT_TIME_MS = 2500;
	private static final int SNAPSHOT_PID_BASE_ID = 999999;

	private final Set<DiagnosticTroubleCode> dtcAccumulator = ConcurrentHashMap.newKeySet();
	private final Map<String, UdsSnapshotResponse> snapshots = new ConcurrentHashMap<>();
	private volatile CountDownLatch snapshotLatch;
	
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

		if (reply.getCommand().getPid().getGroup() == PIDsGroup.DTC_READ) {
			dtcAccumulator.addAll((List<DiagnosticTroubleCode>) reply.getValue());
		}

		if (reply.getValue() instanceof UdsSnapshotResponse) {
			final UdsSnapshotResponse value = (UdsSnapshotResponse) reply.getValue();
			snapshots.put(value.getDtcHex(), value);

			if (snapshotLatch != null) {
				snapshotLatch.countDown();
			}
		}
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		final DiagnosticTroubleCodeScheduleCommand scheduleCommand = (DiagnosticTroubleCodeScheduleCommand) command;
		log.info("Executing DiagnosticTroubleCodeHandler for actions: {}", scheduleCommand.getActions());

		CompletableFuture.runAsync(() -> {
			try {
				long time = System.currentTimeMillis();

				final Set<DiagnosticTroubleCode> dtcValue = drainAccumulator();

				if (dtcValue.isEmpty()) {
					log.warn("DTC polling timed out or no Diagnostic Trouble Codes found.");
					notifySubscribers(Collections.emptySet());
					return;
				}

				log.info("Found Diagnostic Trouble Codes, length: {}.", dtcValue.size());

				if (scheduleCommand.getActions().contains(DtcAction.READ_SNAPSHPOTS)) {
					processSnapshots(dtcValue);
				}

				notifySubscribers(dtcValue);
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

	private void processSnapshots(Set<DiagnosticTroubleCode> dtcValue) throws InterruptedException {

		log.info("Read Snapshots is enabled. Populating new commands to get DTC details.");
		long processingTime = System.currentTimeMillis();

		
		snapshotLatch = new CountDownLatch(dtcValue.size());

		commandBuffer.clear();
		commandProducer.pause();

		for (final DiagnosticTroubleCode dtc : dtcValue) {
			if (log.isDebugEnabled()) {
				log.debug("Adding DTC Snapshot for '{}' to the buffer", dtc.getRawHex());
			}

			final PidDefinition pid = new PidDefinition(SNAPSHOT_PID_BASE_ID, "1904",
					String.format("19 04 %s FF", dtc.getRawHex()), "DTC Read Snapshot", "19",
					DiagnosticTroubleCodeSnapshotCodec.class.getName());

			pidRegistry.register(pid);
			commandBuffer.addLast(new ObdCommand(pid));
		}

		commandProducer.resume();

		final boolean snapshotsComplete = snapshotLatch.await(MAX_WAIT_TIME_MS * 2, TimeUnit.MILLISECONDS);

		if (!snapshotsComplete) {
			log.warn("Timed out waiting for all snapshots to arrive. Expected: {}, Received: {}", dtcValue.size(),
					snapshots.size());
		}

		dtcValue.forEach(dtc -> {
			final UdsSnapshotResponse udsSnapshotResponse = snapshots.get(dtc.getRawHex());
			if (udsSnapshotResponse != null) {
				dtc.setSnapshot(udsSnapshotResponse.getExtractedDids());
			}
		});

		processingTime = System.currentTimeMillis() - processingTime;
		log.info("DTC snapshots were procssing in {}ms", processingTime);
	}

	private void notifySubscribers(Set<DiagnosticTroubleCode> dtcs) {

		log.info ("Notyfing about {} DTCs found", dtcs.size());
		subscription.onDTCCompleted(dtcs, DiagnosticTroubleCodeClearStatus.NO_DATA);
		snapshots.clear();
	}
}