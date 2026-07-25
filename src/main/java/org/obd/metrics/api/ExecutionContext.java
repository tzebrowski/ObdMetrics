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
package org.obd.metrics.api;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.discovery.ModuleDiscoveryCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeScheduleCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.command.routine.RoutineCommand;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
final class ExecutionContext {

	private final ConnectionManager connectionManager;
	private final CommandProducer commandProducer;
	private final Subscription subscription;
	private final CommandsBuffer commandsBuffer;
	private final ConnectorResponseBuffer responseBuffer;
	private final List<Callable<Void>> workerThreads;
	private final Diagnostics diagnostics;
    private final Alerts alerts;
    private final PidDefinitionRegistry registry;
    

	void scheduleDTCAction(Set<DtcAction> actions, List<Init.Header> modules) {

		log.info("[DTC] Workflow is already running. Pausing command producer");

		commandProducer.pause();
		commandsBuffer.clear();

		if (modules == null || modules.isEmpty()) {
			if (actions.contains(DtcAction.CLEAR)) {
				registry.findBy(PIDsGroup.DTC_CLEAR).forEach(c -> {
					log.info("[DTC] Adding DTC clear command {}", c);
					commandsBuffer.addLast(new ObdCommand(c));
				});
			}

			if (actions.contains(DtcAction.READ) || actions.contains(DtcAction.READ_SNAPSHPOTS)) {
				registry.findBy(PIDsGroup.DTC_READ).forEach(c -> {
					log.info("[DTC] Adding DTC read command {}", c);
					commandsBuffer.addLast(new ObdCommand(c));
				});
			}
		} else {
			modules.forEach(module -> {
				// module.getMode() is used here as a CAN module label (eg. "ABS"), not a protocol mode.
				log.info("[DTC] Scheduling DTC action for module '{}', header: {}", module.getMode(), module.getHeader());

				if (module.getHeader() != null && module.getHeader().length() > 0) {
					commandsBuffer.addLast(new ATCommand("SH" + module.getHeader()));
				}

				if (actions.contains(DtcAction.CLEAR)) {
					registry.findBy(PIDsGroup.DTC_CLEAR).forEach(c -> {
						log.info("[DTC] Adding DTC clear command {} for module '{}'", c, module.getMode());
						commandsBuffer.addLast(new ObdCommand(c));
					});
				}

				if (actions.contains(DtcAction.READ) || actions.contains(DtcAction.READ_SNAPSHPOTS)) {
					registry.findBy(PIDsGroup.DTC_READ).forEach(c -> {
						log.info("[DTC] Adding DTC read command {} for module '{}'", c, module.getMode());
						commandsBuffer.addLast(new ObdCommand(c.withModule(module.getMode())));
					});
				}
			});
		}

		log.info("[DTC] Adding DTC schedule command");
		commandsBuffer.addLast(new DiagnosticTroubleCodeScheduleCommand(actions));
		commandProducer.resume();
	}

	void discoverModule(String header) {
		log.info("[Discovery] Probing header {}", header);
		commandProducer.pause();
		commandsBuffer.addLast(new ATCommand("SH" + header));
		commandsBuffer.addLast(new ModuleDiscoveryCommand(header));
		commandProducer.resume();
	}

	WorkflowExecutionStatus executeRoutine(Long routineId, Init init) {
		log.info("[Routine] Workflow is already running. Pausing command producer");
		commandProducer.pause();

		final PidDefinition pid = registry.findBy(routineId);

		if (pid == null || !PIDsGroup.ROUTINE.equals(pid.getGroup())) {
			log.info("[Routine] No routine found or invalid group for given ID={}", routineId);
			return WorkflowExecutionStatus.REJECTED;
		}

		init.getHeaders().stream().filter(w -> w.getMode().equals(pid.deductMode())).findFirst()
				.ifPresent(id -> commandsBuffer.addLast(new ATCommand("SH" + id.getHeader())));

		commandsBuffer.addLast(UDSConstants.UDS_EXTENDED_SESSION);
		commandsBuffer.addLast(UDSConstants.UDS_TESTER_AVAILIBILITY);
		commandsBuffer.addLast(new RoutineCommand(pid));
		commandsBuffer.addLast(UDSConstants.UDS_DEFAULT_SESSION);

		final Adjustments adjustments = Adjustments.DEFAULT;
		log.info("[Routine] Removing cyclic commands from command producer.");
		commandProducer.updateSettings(adjustments, new CommandsSuplier(registry, adjustments, Query.builder().build(), init), diagnostics, init);
		commandProducer.resume();

		return WorkflowExecutionStatus.ROUTINE_QUEUED;
	}

	void updateQuery(Query query, Init init, Adjustments adjustments) {
		log.info("[Update] Workflow is already running. Pausing command producer");
		diagnostics.rate().reset();
		commandProducer.pause();

		commandsBuffer.clear();
		commandsBuffer.addFirst(UDSConstants.UDS_DEFAULT_SESSION);

		final CommandsSuplier commandsSupplier = new CommandsSuplier(registry, adjustments, query, init);
		connectionManager.update(commandsSupplier.get());
		commandProducer.updateSettings(adjustments, commandsSupplier, diagnostics, init);

		log.info("[Update] Resuming command producer");
		commandProducer.resume();
	}

	void stop(boolean gracefulStop) {
		log.info("Publishing onStopping event to let components complete.");
		subscription.onStopping();

		if (!gracefulStop) {
			try {
				log.info("Graceful stop is not enabled. Closing streams by force.");
				connectionManager.getConnector().close();
			} catch (Exception e) {
				subscription.onError("Failed to forcefully close connector", e);
			}
		}

		try {
			log.debug("Deleting existing commands from the buffers.");
			commandsBuffer.clear();
			responseBuffer.clear();
			log.debug("Publishing QUIT command...");
			commandsBuffer.addFirst(new QuitCommand());
		} catch (Exception e) {
			subscription.onError("Failed to clear buffers or add quit command", e);
		}
	}
}