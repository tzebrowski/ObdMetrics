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
package org.obd.metrics.codec.formula;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;

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

	Map<Long, Number> load(final CachePolicy cachePolicy) {
		synchronized (objectMapper) {
			try (final FileInputStream fis = new FileInputStream(cachePolicy.getResultCacheFilePath())) {

				final Map<Long, Number> items = objectMapper.readValue(fis, typeRef);
				log.info("Load cache file from the disk: {}. Found {} entries", cachePolicy.getResultCacheFilePath(),
						items.size());
				return items;
			} catch (final Exception e) {
				log.trace("Failed to load cache from the disk", e);
				log.warn("Failed to load cache from the disk: {}", e.getMessage());
			}
			return Collections.emptyMap();
		}
	}

	void store(final CachePolicy cachePolicy, final Map<Long, Number> items) {
		synchronized (objectMapper) {
			try (final FileOutputStream fos = new FileOutputStream(cachePolicy.getResultCacheFilePath())) {
				log.info("Store cache file from the disk: {}. Number of entries: {} ",
						cachePolicy.getResultCacheFilePath(), items.size());

				objectMapper.writeValue(fos, items);
				fos.flush();
			} catch (final Exception e) {
				log.trace("Failed to store cache on the disk", e);
				log.warn("Failed to store cache on the disk: {}", e.getMessage());
			}
		}
	}
}
