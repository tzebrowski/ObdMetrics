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

public class UdsSnapshotParser {

    public static void main(String[] args) {
        String rawData = "0310:59040191138F1:000B100800016F2:6410090000200A3:340B60821310004:0000181D10AB105:030B19350B18626:FD9E18120010047:82";
        
        UdsSnapshotParser parser = new UdsSnapshotParser();
        UdsSnapshotResponse result = parser.parse(rawData);

        if (result.isError()) {
            System.err.println(result.getErrorMessage());
        } else {
            System.out.println("Parsed Successfully!");
            System.out.println("DTC: " + result.getDtcHex());
            System.out.println("Number of DIDs: " + result.getNumberOfDids());
            System.out.println("Raw Data to decode later: " + result.getRawDataBlock());
        }
    }
	
    public UdsSnapshotResponse parse(String rawMultiFrame) {
        UdsSnapshotResponse response = new UdsSnapshotResponse();

        if (rawMultiFrame == null || rawMultiFrame.trim().isEmpty()) {
            response.setError("Input payload is null or empty.");
            return response;
        }

        String payload = rawMultiFrame.replaceAll("\\s+", "");
        payload = payload.replaceAll("(?:^[0-9A-F]{3,4})?[0-9A-F]:", "");

        if (!payload.startsWith("5904")) {
            response.setError("Not a valid Snapshot response. Payload: " + payload);
            return response;
        }

        // Validate minimum length (Header is 16 characters / 8 bytes)
        // 5904 (4) + DTC (6) + Status (2) + Record# (2) + NumDIDs (2) = 16 chars
        if (payload.length() < 16) {
            response.setError("Payload too short to contain a complete Snapshot Header.");
            return response;
        }

        try {
            response.setDtcHex(payload.substring(4, 10));
            response.setStatusHex(payload.substring(10, 12));
            response.setRecordNumber(Integer.parseInt(payload.substring(12, 14), 16));
            response.setNumberOfDids(Integer.parseInt(payload.substring(14, 16), 16));
            
            response.setRawDataBlock(payload.substring(16));
            
        } catch (NumberFormatException e) {
            response.setError("Failed to parse hex values in the header: " + e.getMessage());
        }

        return response;
    }
}