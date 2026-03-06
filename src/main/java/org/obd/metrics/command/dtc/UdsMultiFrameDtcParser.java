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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obd.metrics.api.model.UdsDtc;

public class UdsMultiFrameDtcParser {

    /**
     * DTO for the overall response
     */
    public static class UdsResponse {
        public String rawPayload;
        public String statusAvailabilityMaskHex;
        public List<String> supportedStatuses = new ArrayList<>();
        public List<UdsDtc> dtcs = new ArrayList<>();
        public String error;

        public boolean hasError() { return error != null; }
    }

    /**
     * Cleans an ELM327 multi-frame string, removes pending messages, 
     * and extracts the continuous hex payload.
     */
    public  String extractPayload(String rawMultiFrame) {
        if (rawMultiFrame == null) return "";

        // Remove any whitespace
        String cleaned = rawMultiFrame.replaceAll("\\s+", "");

        // Strip out UDS "Response Pending" messages (NRC 0x78)
        // e.g., "7F1978" -> removes it completely
        cleaned = cleaned.replaceAll("7F[0-9A-F]{2}78", "");

        // Extract the expected payload length from the First Frame
        int expectedBytes = -1;
        Matcher m = Pattern.compile("^([0-9A-F]{3})0:").matcher(cleaned);
        if (m.find()) {
            expectedBytes = Integer.parseInt(m.group(1), 16);
        }

        // Strip out the multi-frame headers (e.g., "00B0:", "1:")
        String payload = cleaned.replaceAll("(?:^[0-9A-F]{3})?[0-9A-F]:", "");

        // Trim any padding zeroes at the end based on the expected length
        if (expectedBytes > 0 && payload.length() >= expectedBytes * 2) {
            payload = payload.substring(0, expectedBytes * 2);
        }

        return payload;
    }

    /**
     * Parses the reassembled UDS payload.
     */
    public  UdsResponse parse(String rawMultiFrame) {
        UdsResponse response = new UdsResponse();
        
        String payload = extractPayload(rawMultiFrame);
        response.rawPayload = payload;

        if (payload.isEmpty()) {
            response.error = "Payload is empty after extraction.";
            return response;
        }

        // Ensure it's a positive response for Service 0x19, Subfunction 0x02
        if (!payload.startsWith("5902")) {
            response.error = "Not a valid UDS Service $19 02 positive response. Payload: " + payload;
            return response;
        }

        // Extract and decode the Status Availability Mask (Byte 2)
        response.statusAvailabilityMaskHex = payload.substring(4, 6);
        int maskValue = Integer.parseInt(response.statusAvailabilityMaskHex, 16);
        response.supportedStatuses = decodeStatusBits(maskValue);

        // The remaining payload consists of 4-byte chunks (3 bytes DTC + 1 byte Status)
        String dtcData = payload.substring(6);

        for (int i = 0; i < dtcData.length(); i += 8) {
            if (i + 8 <= dtcData.length()) {
                String dtcHex = dtcData.substring(i, i + 6);
                String statusHex = dtcData.substring(i + 6, i + 8);
                
                response.dtcs.add(decodeUdsDtc(dtcHex, statusHex));
            }
        }

        return response;
    }

    /**
     * Decodes a 3-byte UDS DTC into the standard 5-character format and parses the status byte.
     */
    private  UdsDtc decodeUdsDtc(String hex3Bytes, String statusHex) {
        int byte1 = Integer.parseInt(hex3Bytes.substring(0, 2), 16);
        String byte2 = hex3Bytes.substring(2, 4);
        String ftb = hex3Bytes.substring(4, 6); // Failure Type Byte

        // Bitwise extraction for System (Top 2 bits of Byte 1)
        int systemBits = (byte1 >> 6) & 0x03;
        char systemChar = "PCBU".charAt(systemBits);

        // Bitwise extraction for Category (Next 2 bits of Byte 1)
        int categoryBits = (byte1 >> 4) & 0x03;

        // Bitwise extraction for Subsystem (Bottom 4 bits of Byte 1)
        int subsystemBits = byte1 & 0x0F;

        // Assemble standard OBD2 code (e.g., P0191)
        String standardCode = String.format("%c%d%X%s", systemChar, categoryBits, subsystemBits, byte2);
        
        // Parse status
        int statusMask = Integer.parseInt(statusHex, 16);
        List<String> statuses = decodeStatusBits(statusMask);

        return new UdsDtc(standardCode, ftb, hex3Bytes, statusMask, statuses);
    }

    /**
     * Decodes the standard UDS DTC status bitmask. 
     * This works for both the Availability Mask and individual DTC statuses.
     */
    private List<String> decodeStatusBits(int status) {
        final List<String> active = new ArrayList<>();
        if ((status & 0x01) != 0) active.add("Test Failed");
        if ((status & 0x02) != 0) active.add("Test Failed This Operation Cycle");
        if ((status & 0x04) != 0) active.add("Pending DTC");
        if ((status & 0x08) != 0) active.add("Confirmed DTC");
        if ((status & 0x10) != 0) active.add("Test Not Completed Since Last Clear");
        if ((status & 0x20) != 0) active.add("Test Failed Since Last Clear");
        if ((status & 0x40) != 0) active.add("Test Not Completed This Operation Cycle");
        if ((status & 0x80) != 0) active.add("Warning Indicator Requested");
        return active;
    }
}