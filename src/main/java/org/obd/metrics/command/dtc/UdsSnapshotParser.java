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
package org.obd.metrics.command.dtc;

import java.util.Optional;

import org.obd.metrics.api.model.SnapshotPID;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public final class UdsSnapshotParser {
	
    private final SnapshotFormulaEvaluator evaluator;
    private final PidDefinitionRegistry registry;
    
    public UdsSnapshotParser(final PidDefinitionRegistry registry, final String engineName) {
    	this.registry = registry;
    	this.evaluator= new SnapshotFormulaEvaluator(engineName);
	}
    
    
    public UdsSnapshotResponse parse(String rawMultiFrame) {
        UdsSnapshotResponse response = new UdsSnapshotResponse();

        if (rawMultiFrame == null || rawMultiFrame.trim().isEmpty()) {
            response.setErrorMessage("Input payload is null or empty.");
            return response;
        }

        final String payload = rawMultiFrame.replaceAll("\\s+", "").replaceAll("(?:^[0-9A-F]{3,4})?[0-9A-F]:", "");

        if (!payload.startsWith("5904")) {
            response.setErrorMessage("Not a valid Snapshot response. Payload: " + payload);
            return response;
        }

        // Validate minimum length (Header is 16 characters / 8 bytes)
        // 5904 (4) + DTC (6) + Status (2) + Record# (2) + NumDIDs (2) = 16 chars
        if (payload.length() < 16) {
            response.setErrorMessage("Payload too short to contain a complete Snapshot Header.");
            return response;
        }

        try {
            response.setDtcHex(payload.substring(4, 10));
            response.setStatusHex(payload.substring(10, 12));
            response.setRecordNumber(Integer.parseInt(payload.substring(12, 14), 16));
            response.setNumberOfDids(Integer.parseInt(payload.substring(14, 16), 16));
            
            response.setRawDataBlock(payload.substring(16));
            extractAndDecodeDids(response);
           
        } catch (NumberFormatException e) {
            response.setErrorMessage("Failed to parse hex values in the header: " + e.getMessage());
        }

        return response;
    }
    
    private void extractAndDecodeDids(UdsSnapshotResponse response) {
        final String rawDataBlock = response.getRawDataBlock();
        int currentIndex = 0;

        while (currentIndex < rawDataBlock.length() && response.getExtractedDids().size() < response.getNumberOfDids()) {
            
            if (currentIndex + 4 > rawDataBlock.length()) break;
            
            // Read the 2-byte DID (4 hex characters)
            String currentDidHex = rawDataBlock.substring(currentIndex, currentIndex + 4);
            currentIndex += 4; 

            // Look up the PID in the registry
            final Optional<PidDefinition> pidDefOpt = registry.findAll().stream()
                    .filter(p -> p.getPid().equalsIgnoreCase(currentDidHex))
                    .findFirst();

            if (pidDefOpt.isEmpty()) {
                response.setErrorMessage("Unknown DID encountered: " + currentDidHex + ". Halting extraction.");
                break; 
            }

            PidDefinition def = pidDefOpt.get();

            if (def.getLength() <= 0) {
                response.setErrorMessage("PID " + currentDidHex + " has no length defined in JSON! Halting extraction.");
                break;
            }

            int charsToRead = def.getLength() * 2; // 1 byte = 2 hex chars
            
            if (currentIndex + charsToRead > rawDataBlock.length()) {
                response.setErrorMessage("Data block ended unexpectedly while reading DID " + currentDidHex);
                break;
            }

            // Extract value and evaluate
            final String valueHex = rawDataBlock.substring(currentIndex, currentIndex + charsToRead);
            currentIndex += charsToRead;
            
            final Number decodedValue = evaluator.evaluate(def, valueHex);

            response.addSnapshotPID(new SnapshotPID(def, valueHex, decodedValue));
        }
    }
    
}