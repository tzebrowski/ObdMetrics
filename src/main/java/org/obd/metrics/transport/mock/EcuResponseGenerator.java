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
package org.obd.metrics.transport.mock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.obd.metrics.codec.generator.GeneratorPolicy;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EcuResponseGenerator {

    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final PidDefinitionRegistry registry;
    private final Map<String, Double> PID_STATES = new ConcurrentHashMap<>();
    
    // Caches the reverse-calculated formula values: Map<PidId, TreeMap<CalculatedValue, HexPayload>>
    private final Map<Long, TreeMap<Double, String>> REVERSE_LOOKUP_CACHE = new ConcurrentHashMap<>();

    public List<String> generateAnswers(String query, int count, GeneratorPolicy policy) {
        List<String> answers = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            answers.add(generateSingleAnswer(query, policy));
        }
        return answers;
    }

    private String generateSingleAnswer(String query, GeneratorPolicy policy) {
        final String[] tokens = getTokens(query);
        
        String mode = tokens[0];

        int modeInt = Integer.parseInt(mode, 16);
        String responseMode = String.format("%02X", modeInt + 0x40);

        StringBuilder logicalPayload = new StringBuilder(responseMode);

        for (int i = 1; i < tokens.length; i++) {
            String pid = tokens[i];
            logicalPayload.append(pid);
            logicalPayload.append(generateHexForPid(mode, pid, policy)); 
        }

        return formatMultiFrameResponse(logicalPayload.toString(), mode);
    }

    private String[] getTokens(String query) {
        String dataPart;
        int dataIndex = query.indexOf("D:");
        if (dataIndex != -1) {
            dataPart = query.substring(dataIndex + 2).trim();
        } else {
            // Assume raw / non-STN format
            dataPart = query.trim();
        }
        
        // Handle concatenated queries without spaces (e.g., "221935")
        if (!dataPart.contains(" ") && dataPart.length() > 2) {
            String mode = dataPart.substring(0, 2); // First 2 chars are the mode
            String pid = dataPart.substring(2);     // The rest is the PID
            return new String[] { mode, pid };
        }
        
        // Handle spaced queries (e.g., "22 1935 1000")
        String[] tokens = dataPart.split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            throw new IllegalArgumentException("Query is empty or invalid: " + query);
        }
        return tokens;
    }

    private String generateHexForPid(String mode, String pid, GeneratorPolicy policy) {
        if (registry == null) return "0000";

        Optional<PidDefinition> defOpt = registry.findAll().stream()
                .filter(p -> mode.equals(p.getMode()) && pid.equals(p.getPid()))
                .findFirst();

        if (defOpt.isPresent()) {
            PidDefinition definition = defOpt.get();
            if (definition.getLength() <= 0) return "0000";

            if (policy != null && policy.isEnabled()) {
                return applyStrategyWithFormula(definition, policy);
            } else {
                return generatePaddedHex(0, definition.getLength());
            }
        }
        return "0000"; 
    }

    private String applyStrategyWithFormula(PidDefinition pid, GeneratorPolicy policy) {
        String stateKey = pid.getMode() + pid.getPid();
        double min = pid.getMin() != null ? pid.getMin().doubleValue() : 0.0;
        
        // Get next value from strategy
        Double currentValue = PID_STATES.getOrDefault(stateKey, min);
        Double nextValue = policy.getStrategy().getGeneratorStrategy().calculateNext(pid, currentValue);
        PID_STATES.put(stateKey, nextValue);

        // If no formula exists, fallback to linear interpolation
        if (pid.getFormula() == null || pid.getFormula().trim().isEmpty()) {
            return linearInterpolation(nextValue, pid);
        }

        // Find the closest Hex matching the formula's output
        return findClosestHexUsingFormula(pid, nextValue);
    }

    private String findClosestHexUsingFormula(PidDefinition pid, Double targetValue) {
        TreeMap<Double, String> lookupTable = REVERSE_LOOKUP_CACHE.computeIfAbsent(
            pid.getId(), 
            k -> buildFormulaLookupTable(pid)
        );

        Entry<Double, String> floor = lookupTable.floorEntry(targetValue);
        Entry<Double, String> ceiling = lookupTable.ceilingEntry(targetValue);

        if (floor == null && ceiling == null) return generatePaddedHex(0, pid.getLength());
        if (floor == null) return ceiling.getValue();
        if (ceiling == null) return floor.getValue();

        return Math.abs(targetValue - floor.getKey()) < Math.abs(targetValue - ceiling.getKey()) 
                ? floor.getValue() 
                : ceiling.getValue();
    }

    private TreeMap<Double, String> buildFormulaLookupTable(PidDefinition pid) {
        TreeMap<Double, String> lookup = new TreeMap<>();
        int length = pid.getLength();
        
        long maxValue = (length >= 4) ? 0xFFFFFFFFL : (1L << (length * 8)) - 1;
        long step = Math.max(1L, maxValue / 65535L);

        String rawFormula = pid.getFormula();

        for (long i = 0; i <= maxValue; i += step) {
            try {
                long a = length >= 1 ? (i >> (8 * (length - 1))) & 0xFF : 0;
                long b = length >= 2 ? (i >> (8 * (length - 2))) & 0xFF : 0;
                long c = length >= 3 ? (i >> (8 * (length - 3))) & 0xFF : 0;
                long d = length >= 4 ? (i >> (8 * (length - 4))) & 0xFF : 0;
                
                engine.put("A", a);
                engine.put("B", b);
                engine.put("C", c);
                engine.put("D", d);
                engine.eval("var X = undefined;"); 

                Object result = engine.eval(rawFormula);
                
                if (result instanceof Number) {
                    lookup.put(((Number) result).doubleValue(), generatePaddedHex(i, length));
                }
            } catch (ScriptException e) {
                // Ignore script errors for specific inputs
            }
        }
        
        if (lookup.isEmpty()) {
             lookup.put(0.0, generatePaddedHex(0, length));
        }
        
        return lookup;
    }

    private String linearInterpolation(Double value, PidDefinition pid) {
        int length = pid.getLength();
        double min = pid.getMin() != null ? pid.getMin().doubleValue() : 0.0;
        double max = pid.getMax() != null ? pid.getMax().doubleValue() : 255.0;

        if (max <= min) return generatePaddedHex(0, length);
        value = Math.max(min, Math.min(max, value));
        double normalized = (value - min) / (max - min);

        long maxHexValue = (1L << (length * 8)) - 1;
        long calculatedValue = Math.round(normalized * maxHexValue);

        return generatePaddedHex(calculatedValue, length);
    }

    private String generatePaddedHex(long value, int lengthInBytes) {
        String hex = Long.toHexString(value).toUpperCase();
        int requiredChars = lengthInBytes * 2;
        StringBuilder sb = new StringBuilder();
        while (sb.length() + hex.length() < requiredChars) sb.append('0');
        sb.append(hex);
        return sb.toString();
    }

    private String formatMultiFrameResponse(String payload, String originalMode) {
        int totalBytes = payload.length() / 2;
        
        // Single Frame: return the raw payload without the NRC prefix
        if (totalBytes <= 7) {
            return payload;
        }

        // Multi-Frame formatting
        String nrcPrefix = "7F" + originalMode + "78";
        StringBuilder mfResponse = new StringBuilder();
        mfResponse.append(nrcPrefix).append(String.format("%03X", totalBytes)).append("0:").append(payload.substring(0, 12));

        String remainingPayload = payload.substring(12);
        int sequenceNumber = 1;

        while (remainingPayload.length() > 0) {
            int chunkSize = Math.min(14, remainingPayload.length());
            String chunk = remainingPayload.substring(0, chunkSize);
            mfResponse.append(Integer.toHexString(sequenceNumber).toUpperCase()).append(":").append(chunk);
            remainingPayload = remainingPayload.substring(chunkSize);
            sequenceNumber = (sequenceNumber + 1) % 16;
        }
        return mfResponse.toString();
    }
}