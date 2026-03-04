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

import java.nio.file.Path;

import org.agrona.collections.Long2ObjectHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.obd.metrics.api.model.CachePolicy;

public class FormulaEvaluatorCachePersitenceTest {

	@Test
	public void shouldStoreAndLoadCacheSuccessfully(@TempDir Path tempDir) {
		//  Arrange
		String tempFilePath = tempDir.resolve("formula_cache.json").toString();
		
		CachePolicy cachePolicy = CachePolicy.builder()
				.resultCacheEnabled(true)
				.resultCacheSize(100)
				.resultCacheFilePath(tempFilePath)
				.build();

		Long2ObjectHashMap<Number> originalMap = new Long2ObjectHashMap<>(16, 0.65f);
		originalMap.put(123456789, 42.5);
		originalMap.put(987654321, 100);
		originalMap.put(555555555, -15.23);

		FormulaEvaluatorCachePersitence persistence = new FormulaEvaluatorCachePersitence();

		// Act
		persistence.store(cachePolicy, originalMap);
		Long2ObjectHashMap<Number> loadedMap = persistence.load(cachePolicy);

		// Assert
		Assertions.assertThat(loadedMap).isNotNull();
		Assertions.assertThat(loadedMap.size()).isEqualTo(3);
		
		// Note: Jackson deserializes raw numbers into Doubles or Integers automatically
		Assertions.assertThat(loadedMap.get(123456789L).doubleValue()).isEqualTo(42.5);
		Assertions.assertThat(loadedMap.get(987654321L).intValue()).isEqualTo(100);
		Assertions.assertThat(loadedMap.get(555555555L).doubleValue()).isEqualTo(-15.23);
	}

	@Test
	public void shouldReturnEmptyMapWhenFileDoesNotExist(@TempDir Path tempDir) {
		// Arrange
		// Pointing to a file that we deliberately never create
		String missingFilePath = tempDir.resolve("missing_cache.json").toString();
		
		CachePolicy cachePolicy = CachePolicy.builder()
				.resultCacheEnabled(true)
				.resultCacheFilePath(missingFilePath)
				.build();

		FormulaEvaluatorCachePersitence persistence = new FormulaEvaluatorCachePersitence();

		// Act
		Long2ObjectHashMap<Number> loadedMap = persistence.load(cachePolicy);

		// Assert
		Assertions.assertThat(loadedMap).isNotNull();
		Assertions.assertThat(loadedMap.isEmpty()).isTrue();
	}

	@Test
	public void shouldStoreAndLoadEmptyMapGracefully(@TempDir Path tempDir) {
		// Arrange
		String tempFilePath = tempDir.resolve("empty_cache.json").toString();
		
		CachePolicy cachePolicy = CachePolicy.builder()
				.resultCacheEnabled(true)
				.resultCacheFilePath(tempFilePath)
				.build();

		Long2ObjectHashMap<Number> emptyMap = new Long2ObjectHashMap<>(16, 0.65f);
		FormulaEvaluatorCachePersitence persistence = new FormulaEvaluatorCachePersitence();

		// Act
		persistence.store(cachePolicy, emptyMap);
		Long2ObjectHashMap<Number> loadedMap = persistence.load(cachePolicy);

		// Assert
		Assertions.assertThat(loadedMap).isNotNull();
		Assertions.assertThat(loadedMap.isEmpty()).isTrue();
	}
}