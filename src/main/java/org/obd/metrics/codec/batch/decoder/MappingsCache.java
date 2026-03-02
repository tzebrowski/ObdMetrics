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
 * Uses linked nodes and loop unrolling to approach pure Map lookup speeds.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MappingsCache {

    private static final int MAX_ENTRIES = 100;

    /**
     * Linked list node avoids array allocation and boundary checks during iteration.
     */
    @RequiredArgsConstructor
    private static final class CacheNode {
        final int[] trimmedColons;
        final Map<ObdCommand, ConnectorResponse> mapping;
        final CacheNode next;
    }

    private final Map<String, CacheNode> mappings = new ConcurrentHashMap<>();

    Map<ObdCommand, ConnectorResponse> lookup(final String query, final int[] colons) {
        if (query == null) {
        	return null;
        }
        
    	CacheNode node = mappings.get(query);

        // Iterate the linked list (usually just 1 node, meaning zero loop overhead)
        while (node != null) {
            if (matchFast(node.trimmedColons, colons)) {
                return node.mapping;
            }
            node = node.next;
        }

        if (log.isDebugEnabled()) {
            log.debug("No mapping found for query: {}", query);
        }

        return null;
    }

    /**
     * Loop unrolling for extreme L1 cache speed.
     * Bypasses the JVM's loop counter and branch prediction penalties.
     */
    private boolean matchFast(final int[] cached, final int[] input) {
        final int len = cached.length;
        
        // Ensure input doesn't have MORE valid colons than the cached array
        if (input.length > len && input[len] != -1) {
            return false;
        }

        // Unrolled checks. 
        // If the input has FEWER valid colons, the -1 padding will naturally fail the equality check.
        switch (len) {
            case 0: return true;
            case 1: return cached[0] == input[0];
            case 2: return cached[0] == input[0] && cached[1] == input[1];
            case 3: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2];
            case 4: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3];
            case 5: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4];
            case 6: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5];
            case 7: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6];
            case 8: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7];
            		
            case 9: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7] && cached[8] == input[8];

            case 10: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7] && cached[8] == input[8] && cached[9] == input[9];

            case 11: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7] && cached[8] == input[8] 
            		&& cached[9] == input[9] && cached[10] == input[10];

            case 12: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7] && cached[8] == input[8] 
            		&& cached[9] == input[9] && cached[10] == input[10] && cached[11] == input[11];

            case 13: return cached[0] == input[0] && cached[1] == input[1] && cached[2] == input[2] && cached[3] == input[3] && cached[4] == input[4]
            		&& cached[5] == input[5] && cached[6] == input[6] && cached[7] == input[7] && cached[8] == input[8] 
            		&& cached[9] == input[9] && cached[10] == input[10] && cached[11] == input[11] && cached[12] == input[12];

            default:
                // Fallback for unusually fragmented frames
                for (int i = 0; i < len; i++) {
                    if (cached[i] != input[i]) return false;
                }
                return true;
        }
    }

    void insert(final String query, final int[] colons, Map<ObdCommand, ConnectorResponse> mapping) {
    	if (query == null) {
        	return;
        }
        
    	if (mappings.size() >= MAX_ENTRIES) {
            mappings.clear();
        }

        int validLength = 0;
        while (validLength < colons.length && colons[validLength] != -1) {
            validLength++;
        }

        final int[] trimmedColons = Arrays.copyOf(colons, validLength);
        
        mappings.compute(query, (k, existingNode) -> {
            // Check for duplicates before adding
            CacheNode current = existingNode;
            while (current != null) {
                if (Arrays.equals(current.trimmedColons, trimmedColons)) {
                    return existingNode;
                }
                current = current.next;
            }
            
            // Prepend new node to the linked list
            return new CacheNode(trimmedColons, mapping, existingNode);
        });
    }
}