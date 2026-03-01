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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Highly optimized, memory-safe LRU Cache implementation.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MappingsCache {

	/**
	 * A lightweight key object to replace expensive String concatenation. This
	 * relies purely on memory references and math, creating zero String
	 * allocations.
	 */
	private static final class CacheKey {
		private final String query;
		private final int[] colons;
		private final int hashCode;

		CacheKey(String query, int[] colons) {
			this.query = query;
			this.colons = colons;
			// Pre-compute hashcode since the key is immutable
			this.hashCode = Objects.hash(query, Arrays.hashCode(colons));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			CacheKey cacheKey = (CacheKey) o;
			return query.equals(cacheKey.query) && Arrays.equals(colons, cacheKey.colons);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	// Maximum number of templates to keep in memory to prevent OutOfMemory errors
	private static final int MAX_ENTRIES = 100;

	// Thread-safe wrapper around an LRU LinkedHashMap
	private final Map<CacheKey, Map<ObdCommand, ConnectorResponse>> mappings = new LinkedHashMap<CacheKey, Map<ObdCommand, ConnectorResponse>>(
			16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<CacheKey, Map<ObdCommand, ConnectorResponse>> eldest) {
			return size() > MAX_ENTRIES; // Evict oldest items automatically
		}
	};

	/**
	 * Looks up the mapping. Returns null if not found. Replaces the need to call
	 * contains() first.
	 */
	synchronized Map<ObdCommand, ConnectorResponse> lookup(final String query, final int[] colons) {
		final CacheKey key = new CacheKey(query, colons);
		final Map<ObdCommand, ConnectorResponse> mapping = mappings.get(key);

		if (mapping == null && log.isDebugEnabled()) {
			log.debug("No mapping found for query: {}", query);
		}

		return mapping;
	}

	synchronized void insert(final String query, final int[] colons, Map<ObdCommand, ConnectorResponse> mapping) {
		mappings.put(new CacheKey(query, colons), mapping);
	}
}