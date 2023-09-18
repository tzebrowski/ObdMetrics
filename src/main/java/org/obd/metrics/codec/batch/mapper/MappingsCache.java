/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.codec.batch.mapper;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Naive cache implementation
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MappingsCache {

	private final Map<String, BatchMessageMapping> mappings = new HashMap<>();

	BatchMessageMapping lookup(String query) {
		final BatchMessageMapping mapping = mappings.get(query);
		if (mapping == null) {
			log.error("no mapping found for {}", query);
			return null;
		}
		mapping.updateCacheHit();
		return mapping;
	}

	boolean contains(String query) {
		return mappings.containsKey(query);
	}

	void insert(String query, BatchMessageMapping mapping) {
		mappings.put(query, mapping);
	}

	int getCacheHit(final String query) {
		return mappings.get(query).getHit();
	}
}
