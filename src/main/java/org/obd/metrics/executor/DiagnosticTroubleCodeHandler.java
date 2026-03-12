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

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.obd.metrics.api.CommandProducer;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeSnapshotCodec;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeScheduleCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class DiagnosticTroubleCodeHandler implements CommandHandler {
	private static final int DELAY_MS = 100;
	private static final int MAX_PULL_ATTEMPTS = 25;

	private final DiagnosticTroubleCodeReader diagnosticTroubleCodeReader = new DiagnosticTroubleCodeReader();
	private final DiagnosticTroubleCodeCleaner diagnosticTroubleCodeCleaner = new DiagnosticTroubleCodeCleaner();

	DiagnosticTroubleCodeHandler() {
		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(diagnosticTroubleCodeReader);
			p.subscribe(diagnosticTroubleCodeCleaner);
		});
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		final DiagnosticTroubleCodeScheduleCommand diagnosticTroubleCodeScheduleCommand = (DiagnosticTroubleCodeScheduleCommand) command;

		log.info("Executing DiagnosticTroubleCodeHandler for actions: {}",
				diagnosticTroubleCodeScheduleCommand.getActions());

		CompletableFuture.runAsync(() -> {
			Set<DiagnosticTroubleCode> dtcValue = null;

			for (int i = 0; i < MAX_PULL_ATTEMPTS; i++) {
				dtcValue = diagnosticTroubleCodeReader.getValue();
				if (dtcValue != null) {
					break;
				}

				try {
					Thread.sleep(DELAY_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("DTC polling thread was interrupted", e);
					break;
				}
			}

			if (dtcValue == null) {
				log.warn("DTC polling timed out. No Diagnostic Trouble Codes found.");
			} else {
				log.info("Found Diagnostic Trouble Codes, length: {}.", dtcValue.size());
				log.info("Status of the Diagnostic Trouble Codes cleanup: {}.",
						diagnosticTroubleCodeCleaner.getValue());

				final Set<DiagnosticTroubleCode> finalDtcValue = dtcValue;
				final DiagnosticTroubleCodeClearStatus finalCleanerValue = diagnosticTroubleCodeCleaner.getValue();

				if (diagnosticTroubleCodeScheduleCommand.getActions().contains(DtcAction.READ_SNAPSHPOTS)) {
					try {

						log.info("Read Snapshots is enabled. Populating new commands to get DTC details.");
						final Context context = Context.instance();
						final CommandsBuffer commandBuffer = context.forceResolve(CommandsBuffer.class);
						final CommandProducer commandProducer = context.forceResolve(CommandProducer.class);
						final PidDefinitionRegistry pidRegistry = context.forceResolve(PidDefinitionRegistry.class);

						commandBuffer.clear();
						commandProducer.pause();
						
						finalDtcValue.forEach(dtc -> {
							log.info("Adding DTC Snaphost for '{}' to the buffer", dtc.getRawHex());

							final PidDefinition pid = new PidDefinition(999999, "1904",
									String.format("19 04 %s FF", dtc.getRawHex()), "DTC Read Snapshot", "19",
									DiagnosticTroubleCodeSnapshotCodec.class.getName());

							pidRegistry.register(pid);
							commandBuffer.addLast(new ObdCommand(pid));
						});

						commandProducer.resume();
					} catch (Throwable e) {
						log.error("Failed to schedule dtc read snsphot rules.");
					}
				}

				Context.apply(ctx -> {
					ctx.resolve(Subscription.class).apply(p -> {
						p.onDTCCompleted(finalDtcValue, finalCleanerValue);
						diagnosticTroubleCodeReader.reset();
					});
				});
			}
		});

		return CommandExecutionStatus.OK;
	}
}
