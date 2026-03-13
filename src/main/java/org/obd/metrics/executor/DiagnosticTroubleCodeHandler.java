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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class DiagnosticTroubleCodeHandler extends ReplyObserver<ObdMetric> implements CommandHandler {

	private static final int MAX_WAIT_TIME_MS = 1500;
	private static final int SNAPSHOT_PID_BASE_ID = 999999;

	private final BlockingQueue<Set<DiagnosticTroubleCode>> dtcQueue = new ArrayBlockingQueue<>(1);
	private final Map<String, UdsSnapshotResponse> snapshots = new ConcurrentHashMap<>();

	private volatile CountDownLatch snapshotLatch;

	DiagnosticTroubleCodeHandler() {
		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(this);
		});
	}

	@Override
	public void onNext(ObdMetric reply) {

		if (reply.getCommand().getPid().getGroup() == PIDsGroup.DTC_READ) {
			dtcQueue.offer(new HashSet<>((List<DiagnosticTroubleCode>) reply.getValue()));
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

				final Set<DiagnosticTroubleCode> dtcValue = dtcQueue.poll(MAX_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
			
				if (dtcValue == null || dtcValue.isEmpty()) {
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

	private void processSnapshots(Set<DiagnosticTroubleCode> dtcValue) throws InterruptedException {

		log.info("Read Snapshots is enabled. Populating new commands to get DTC details.");
		long processingTime = System.currentTimeMillis();

		final Context context = Context.instance();
		final CommandsBuffer commandBuffer = context.forceResolve(CommandsBuffer.class);
		final CommandProducer commandProducer = context.forceResolve(CommandProducer.class);
		final PidDefinitionRegistry pidRegistry = context.forceResolve(PidDefinitionRegistry.class);

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

		dtcValue.forEach(dtc -> dtc.setSnapshot(snapshots.get(dtc.getRawHex())));

		processingTime = System.currentTimeMillis() - processingTime;
		log.info("DTC snapshots were procssing in {}ms", processingTime);
	}

	private void notifySubscribers(Set<DiagnosticTroubleCode> finalDtcValue) {

		Context.apply(ctx -> {
			ctx.resolve(Subscription.class).apply(p -> {
				p.onDTCCompleted(finalDtcValue, DiagnosticTroubleCodeClearStatus.NO_DATA);
			});
		});
		snapshots.clear();
	}
}