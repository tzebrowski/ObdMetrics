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
package org.obd.metrics.codec.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class EcuMockGenerator {

    private static final Random RANDOM = new Random();

    /**
     * Generates a list of random ECU answers for a given query.
     *
     * @param query The STPX query (e.g., "STPX H:18DA10F1, D:22 1000 1924 ...")
     * @param count The number of random answers to generate
     * @return List of formatted multi-frame ELM327 responses
     */
    public static List<String> generateAnswers(String query, int count) {
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            answers.add(generateSingleAnswer(query));
        }
        return answers;
    }

    private static String generateSingleAnswer(String query) {
        // 1. Extract the data payload from the STPX command
        // Finds "D:22 1000 1924..." and splits it into tokens
        int dataIndex = query.indexOf("D:");
        if (dataIndex == -1) {
            throw new IllegalArgumentException("Query must contain 'D:' data identifier.");
        }
        
        String dataPart = query.substring(dataIndex + 2).trim();
        String[] tokens = dataPart.split("\\s+");
        String mode = tokens[0]; // e.g., "22"

        // 2. Calculate positive response mode (e.g., Mode 22 -> 62)
        int modeInt = Integer.parseInt(mode, 16);
        String responseMode = String.format("%02X", modeInt + 0x40);

        // 3. Build the logical continuous hex payload
        StringBuilder logicalPayload = new StringBuilder(responseMode);

        // Iterate through requested PIDs and append random data
        for (int i = 1; i < tokens.length; i++) {
            String pid = tokens[i];
            logicalPayload.append(pid);
            
            // Simulating 2 bytes (4 hex characters) of random data per PID
            // If your PIDs have variable lengths, you can adjust this logic
            logicalPayload.append(generateRandomHex(4)); 
        }

        // 4. Format into ELM327 / STN multi-frame structure
        return formatMultiFrameResponse(logicalPayload.toString(), mode);
    }

    private static String formatMultiFrameResponse(String payload, String originalMode) {
        int totalBytes = payload.length() / 2;

        // NRC Prefix (7F [Mode] 78) - ECU is busy / response pending
        String nrcPrefix = "7F" + originalMode + "78";

        // If the payload fits in a Single Frame (<= 7 bytes)
        if (totalBytes <= 7) {
            return nrcPrefix + payload;
        }

        // Multi-Frame Formatting
        StringBuilder mfResponse = new StringBuilder();
        mfResponse.append(nrcPrefix);
        
        // Add total payload length (e.g., "04E")
        mfResponse.append(String.format("%03X", totalBytes));
        
        // First Frame (0:) takes the first 6 bytes (12 hex chars) of the payload
        mfResponse.append("0:");
        mfResponse.append(payload.substring(0, 12));

        String remainingPayload = payload.substring(12);
        int sequenceNumber = 1; // Consecutive frames start at 1:

        // Chunk the rest of the payload into Consecutive Frames (up to 7 bytes / 14 chars each)
        while (remainingPayload.length() > 0) {
            int chunkSize = Math.min(14, remainingPayload.length());
            String chunk = remainingPayload.substring(0, chunkSize);
            
            mfResponse.append(Integer.toHexString(sequenceNumber).toUpperCase()).append(":");
            mfResponse.append(chunk);

            remainingPayload = remainingPayload.substring(chunkSize);
            
            // Sequence rolls over from F to 0 (0, 1, 2... E, F, 0, 1...)
            sequenceNumber = (sequenceNumber + 1) % 16;
        }

        return mfResponse.toString();
    }

    private static String generateRandomHex(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(RANDOM.nextInt(16)).toUpperCase());
        }
        return sb.toString();
    }
}