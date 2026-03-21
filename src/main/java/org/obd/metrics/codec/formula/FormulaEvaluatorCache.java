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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.agrona.collections.Long2ObjectHashMap;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.VehicleCapabilities;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorCache implements Lifecycle {

	private final CachePolicy config;
	
	// Single-threaded, zero-allocation primitive map. 
	// No volatile reads, no memory barriers, pure speed.
	private final Long2ObjectHashMap<Number> cache;
	
	private final FormulaEvaluatorCachePersitence persitence = new FormulaEvaluatorCachePersitence();

	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	FormulaEvaluatorCache(final CachePolicy cachePolicy) {
		this.config = cachePolicy;
		
		this.cache = new Long2ObjectHashMap<>(
				cachePolicy.isResultCacheEnabled() ? cachePolicy.getResultCacheSize() : 16, 0.75f);
	}

	@Override
	public void onStopping() {
		if (config.isResultCacheEnabled() && config.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.info("Saving cache to the disk: {} file. {} items to save.", config.getResultCacheFilePath(), cache.size());
				
				persitence.store(config, cache);
				
				t = System.currentTimeMillis() - t;
				log.info("Saved cache to the disk: {} file. Time: {}ms", config.getResultCacheFilePath(), t);
			};
			singleTaskPool.execute(task);
		}
	}

	@Override
	public void onRunning(final VehicleCapabilities vehicleCapabilities) {
		if (config.isResultCacheEnabled() && config.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.debug("Loading cache from disk: {}", config.getResultCacheFilePath());
				
				cache.putAll(persitence.load(config));
				
				t = System.currentTimeMillis() - t;
				log.debug("Cache was load from the disk. Time: {}ms", t);
			};
			singleTaskPool.execute(task);
		}
	}

	/**
	 * Single-threaded lookup. Bypasses all autoboxing and concurrency overhead.
	 */
	Number computeIfAbsent(final ConnectorResponse connectorResponse, final Supplier<Number> computer) {
		if (!config.isResultCacheEnabled() || !connectorResponse.isCacheable()) {
			return computer.get();
		}

		final long id = connectorResponse.id();
		
		Number result = cache.get(id);

		if (result == null) {
			if (log.isDebugEnabled()) {
				log.debug("Cache miss for ID {}. Computing via ScriptEngine.", id);
			}
			result = computer.get();
			
			if (result != null) { 
				// Primitive PUT
				cache.put(id, result);
			}
		}

		return result;
	}
}