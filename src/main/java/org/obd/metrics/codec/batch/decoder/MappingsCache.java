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
package org.obd.metrics.codec.batch.decoder;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hyper-optimized, zero-allocation, lock-free cache.
 * Trims padded (-1) mutable buffers to ensure $O(1)$ equivalent lookup times.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MappingsCache {

    private static final int MAX_ENTRIES = 100;

    @RequiredArgsConstructor
    private static final class ColonEntry {
        final int[] colons; // Stores ONLY the valid colons, stripped of -1 padding
        final Map<ObdCommand, ConnectorResponse> mapping;
    }

    private final Map<String, ColonEntry[]> mappings = new ConcurrentHashMap<>();

    Map<ObdCommand, ConnectorResponse> lookup(final String query, final int[] colons) {
    	if (query == null) {
        	return null;
        }
        
    	final ColonEntry[] entries = mappings.get(query);

        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                final int[] cachedColons = entries[i].colons;
                final int validLen = cachedColons.length;
                
                boolean match = true;
                
                // Only loop over the valid elements (usually just 1-3 iterations)
                for (int j = 0; j < validLen; j++) {
                    if (cachedColons[j] != colons[j]) {
                        match = false;
                        break;
                    }
                }
                
                // If valid elements match, ensure the incoming buffer actually ends here
                // It must either be at the end of the array, or the next element must be the -1 pad
                if (match && (colons.length == validLen || colons[validLen] == -1)) {
                    return entries[i].mapping;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No mapping found for query: {}", query);
        }

        return null;
    }

    void insert(final String query, final int[] colons, Map<ObdCommand, ConnectorResponse> mapping) {
        if (query == null) {
        	return;
        }
        
    	if (mappings.size() >= MAX_ENTRIES) {
            mappings.clear();
        }

        // Find exactly how many valid colons there are before the -1 padding
        int validLength = 0;
        while (validLength < colons.length && colons[validLength] != -1) {
            validLength++;
        }

        // Store ONLY the valid colons. This prevents memory leaks and guarantees ultra-fast loops.
        final int[] trimmedColons = Arrays.copyOf(colons, validLength);
        final ColonEntry newEntry = new ColonEntry(trimmedColons, mapping);
        
        mappings.compute(query, (k, existingEntries) -> {
            if (existingEntries == null) {
                return new ColonEntry[] { newEntry };
            }
            
            for (int i = 0; i < existingEntries.length; i++) {
                if (Arrays.equals(existingEntries[i].colons, trimmedColons)) {
                    return existingEntries;
                }
            }
            
            ColonEntry[] newArray = Arrays.copyOf(existingEntries, existingEntries.length + 1);
            newArray[existingEntries.length] = newEntry;
            return newArray;
        });
    }
}