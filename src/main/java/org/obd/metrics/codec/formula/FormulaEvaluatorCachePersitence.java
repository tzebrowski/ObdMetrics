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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.agrona.collections.Long2ObjectHashMap;
import org.obd.metrics.api.model.CachePolicy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluatorCachePersitence {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final TypeReference<Map<Long, Number>> typeRef = new TypeReference<Map<Long, Number>>() {
	};

	Long2ObjectHashMap<Number> load(final CachePolicy cachePolicy) {

		final int initialCapacity = cachePolicy.isResultCacheEnabled() ? cachePolicy.getResultCacheSize() : 16;
		final Long2ObjectHashMap<Number> agronaMap = new Long2ObjectHashMap<>(initialCapacity, 0.65f);

		synchronized (objectMapper) {
			try (final FileInputStream fis = new FileInputStream(cachePolicy.getResultCacheFilePath())) {

				final Map<Long, Number> items = objectMapper.readValue(fis, typeRef);

				if (items != null) {
					for (Map.Entry<Long, Number> entry : items.entrySet()) {
						agronaMap.put(entry.getKey().longValue(), entry.getValue());
					}
				}

				log.info("Loaded cache file from the disk: {}. Found {} entries", cachePolicy.getResultCacheFilePath(),
						agronaMap.size());

			} catch (final Exception e) {
				log.trace("Failed to load cache from the disk", e);
				log.warn("Failed to load cache from the disk: {}", e.getMessage());
			}
			return agronaMap;
		}
	}

	void store(final CachePolicy cachePolicy, final Long2ObjectHashMap<Number> cache) {
		synchronized (objectMapper) {
			try (final FileOutputStream fos = new FileOutputStream(cachePolicy.getResultCacheFilePath())) {
				log.info("Storing cache file to the disk: {}. Number of entries: {} ",
						cachePolicy.getResultCacheFilePath(), cache.size());

				objectMapper.writeValue(fos, cache);
				fos.flush();
			} catch (final Exception e) {
				log.trace("Failed to store cache on the disk", e);
				log.warn("Failed to store cache on the disk: {}", e.getMessage());
			}
		}
	}
}