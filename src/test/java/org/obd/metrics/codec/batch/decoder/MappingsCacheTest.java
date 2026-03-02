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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

class MappingsCacheTest {

    private MappingsCache cache;
    private Map<ObdCommand, ConnectorResponse> mockMapping1;
    private Map<ObdCommand, ConnectorResponse> mockMapping2;

    @BeforeEach
    void setup() {
        cache = new MappingsCache();
        
        // Setup mock mappings to verify exact object references are returned
        mockMapping1 = new HashMap<>();
        mockMapping1.put(mock(ObdCommand.class), mock(ConnectorResponse.class));

        mockMapping2 = new HashMap<>();
        mockMapping2.put(mock(ObdCommand.class), mock(ConnectorResponse.class));
    }

    @Test
    void shouldReturnNullForNullQuery() {
        cache.insert("010C", new int[]{1, 2}, mockMapping1);
        
        assertThat(cache.lookup(null, new int[]{1, 2})).isNull();
    }

    @Test
    void shouldNotCrashOnNullInsert() {
        cache.insert(null, new int[]{1, 2}, mockMapping1);
        
        // If it didn't throw a NullPointerException, the null check worked.
        assertThat(cache.lookup("010C", new int[]{1, 2})).isNull();
    }

    @Test
    void shouldFindMappingWithExactColons() {
        final String query = "010C";
        final int[] colons = { 4, 17, 25 };

        cache.insert(query, colons, mockMapping1);

        final Map<ObdCommand, ConnectorResponse> result = cache.lookup(query, colons);
        assertThat(result).isSameAs(mockMapping1);
    }

    @Test
    void shouldTrimPaddingAndMatchPaddedInput() {
        final String query = "010C";
        
        // The transport layer provides a buffer padded with -1
        final int[] insertColons = { 4, 17, -1, -1, -1 };
        cache.insert(query, insertColons, mockMapping1);

        // A new buffer comes in with the same valid colons, but different padding length
        final int[] lookupColons = { 4, 17, -1, -1 };
        
        final Map<ObdCommand, ConnectorResponse> result = cache.lookup(query, lookupColons);
        assertThat(result).isSameAs(mockMapping1);
    }

    @Test
    void shouldNotMatchIfValidColonsDiffer() {
        final String query = "010C";
        
        cache.insert(query, new int[]{ 4, 17 }, mockMapping1);

        // Different colons representing a shifted multi-frame message
        final Map<ObdCommand, ConnectorResponse> result = cache.lookup(query, new int[]{ 4, 25 });
        assertThat(result).isNull();
    }

    @Test
    void shouldNotMatchIfInputHasMoreValidColons() {
        final String query = "010C";
        
        cache.insert(query, new int[]{ 4, 17, -1 }, mockMapping1);

        // Input has 3 valid colons instead of 2
        final Map<ObdCommand, ConnectorResponse> result = cache.lookup(query, new int[]{ 4, 17, 25, -1 });
        assertThat(result).isNull();
    }

    @Test
    void shouldStoreMultipleColonConfigurationsForSameQuery() {
        final String query = "0C 11 0E"; // The multi-frame edge case
        final int[] frameConfig1 = { 4, 25, -1 };
        final int[] frameConfig2 = { 4, 17, -1 };

        // Insert both configurations under the same query
        cache.insert(query, frameConfig1, mockMapping1);
        cache.insert(query, frameConfig2, mockMapping2);

        // Look them both up to ensure the linked list traversal works
        assertThat(cache.lookup(query, frameConfig1)).isSameAs(mockMapping1);
        assertThat(cache.lookup(query, frameConfig2)).isSameAs(mockMapping2);
    }

    @Test
    void shouldPreventDuplicateNodeInsertions() {
        final String query = "010C";
        final int[] colons = { 4, 17 };

        // Insert exactly the same data twice
        cache.insert(query, colons, mockMapping1);
        cache.insert(query, colons, mockMapping2); // This should be ignored

        // Lookup should return the FIRST mapping because deduplication prevented the second insert
        assertThat(cache.lookup(query, colons)).isSameAs(mockMapping1);
    }

    @Test
    void shouldClearCacheWhenMaxEntriesExceeded() {
        final int maxEntries = 100;

        // Fill the cache to its limit
        for (int i = 0; i < maxEntries; i++) {
            cache.insert("QUERY_" + i, new int[]{ 1, 2 }, mockMapping1);
        }

        // Verify the first item is in the cache
        assertThat(cache.lookup("QUERY_0", new int[]{ 1, 2 })).isSameAs(mockMapping1);

        // Insert the 101st item, triggering eviction
        cache.insert("QUERY_100", new int[]{ 1, 2 }, mockMapping2);

        // The first item should now be gone because the cache was cleared
        assertThat(cache.lookup("QUERY_0", new int[]{ 1, 2 })).isNull();
        
        // The new item should be present
        assertThat(cache.lookup("QUERY_100", new int[]{ 1, 2 })).isSameAs(mockMapping2);
    }

    @Test
    void shouldMatchLongUnrolledArrays() {
        final String query = "LONG_FRAME";
        
        // Testing the switch statement fallback boundary (e.g., 14 items)
        final int[] longColons = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, -1 };
        
        cache.insert(query, longColons, mockMapping1);
        
        assertThat(cache.lookup(query, longColons)).isSameAs(mockMapping1);
    }
}