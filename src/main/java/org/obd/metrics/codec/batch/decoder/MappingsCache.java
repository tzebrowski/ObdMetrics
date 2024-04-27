/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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
package org.obd.metrics.codec.batch.decoder;

import java.util.Arrays;
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

	private final Map<String, BatchMessagePositionTemplate> mappings = new HashMap<>();

	BatchMessagePositionTemplate lookup(final String query, final int[] delimeters) {
		final String key = toKey(query, delimeters);
		final BatchMessagePositionTemplate mapping = mappings.get(key);
		
		if (mapping == null) {
			log.error("no mapping found for {}", key);
			return null;
		}
		
		return mapping;
	}

	boolean contains(final String query, final int[] delimeters) {
		return mappings.containsKey(toKey(query, delimeters));
	}

	void insert(final String query, final int[] delimeters, BatchMessagePositionTemplate mapping) {
		mappings.put(toKey(query, delimeters), mapping);
	}

	private String toKey(final String query, final int[] delimeters) {
		return query + Arrays.toString(delimeters);
	}
}
