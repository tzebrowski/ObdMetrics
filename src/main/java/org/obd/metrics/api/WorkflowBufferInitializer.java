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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class WorkflowBufferInitializer {
	private final ExecutionContext activeContext;

    void initialize(Init init, Adjustments adjustments, SniffingPolicy sniffingPolicy) {
        log.info("Initializing CommandsBuffer. Sniffing enabled: {}", 
            (sniffingPolicy != null && sniffingPolicy.isEnabled()));
        
        final CommandsBuffer commandsBuffer = activeContext.getCommandsBuffer();
        commandsBuffer.clear();
        init.getSequence().getCommands().forEach(c -> {
            if (c instanceof DelayCommand) {
                ((DelayCommand) c).setDelay(init.getDelayAfterReset());
            }
        });
        commandsBuffer.add(init.getSequence());

        if (adjustments.getSniffing() != null) {
            SniffingSupport.updateCommandBuffer(adjustments.getSniffing(), commandsBuffer);
        }

        commandsBuffer.addLast(new ATCommand("SP" + init.getProtocol().getType()));

        final PidDefinitionRegistry registry = activeContext.getRegistry();
        
        if (sniffingPolicy != null && sniffingPolicy.isEnabled()) {
            final PidDefinition sniffingPID = SniffingSupport.pid(sniffingPolicy);
            registry.register(sniffingPID);
            commandsBuffer.addLast(new ObdCommand(sniffingPID));
        } else {
            // Standard Query Mode
            adjustments.getRequestedGroups().forEach(group -> {
                final List<Command> commands = registry.findBy(group).stream()
                        .filter(PidDefinition::getStable)
                        .map(ObdCommand::new)
                        .collect(Collectors.toList());
                final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init, commandsBuffer);
                headerManager.testSingleMode(commands);
                commands.forEach(command -> {
                    headerManager.switchHeader(command);
                    commandsBuffer.addLast(command);
                });
            });
        }

        commandsBuffer.addLast(new DelayCommand(init.getDelayAfterInit()));
        commandsBuffer.addLast(new InitCompletedCommand());
    }

    void debugPIDs(Query query, Init init, Adjustments adjustments) {
        if (query == null || query.getPids().isEmpty()) return;
        final Map<String, Header> canHeaders = init.getHeaders().stream()
                .collect(Collectors.toMap(Header::getMode, Function.identity()));
        final ObjectMapper objMapper = new ObjectMapper();
        final PidDefinitionRegistry registry = activeContext.getRegistry();
        
        query.getPids().forEach(id -> {
            final PidDefinition pid = registry.findBy(id);
            if (pid != null) {
                String mode = (pid.getOverrides().getCanMode() != null && !pid.getOverrides().getCanMode().isEmpty()) 
                    ? pid.getOverrides().getCanMode() : pid.getMode();
                String header = canHeaders.containsKey(mode) ? canHeaders.get(mode).getHeader() : "";
                log.info("Mapping PID=[{}] to mode={}, header={}", id, mode, header);
                if (adjustments != null && adjustments.isDebugEnabled()) {
                    try {
                        log.info("PID Definition: {}", objMapper.writeValueAsString(pid));
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to serialize PID");
                    }
                }
            }
        });
    }
}