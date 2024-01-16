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
package org.obd.metrics.codec.formula;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.VehicleCapabilities;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorCache implements Lifecycle {

	private final CachePolicy config;
	private final Map<Long, Number> storage;
	private final FormulaEvaluatorCachePersitence persitence = new FormulaEvaluatorCachePersitence();

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	FormulaEvaluatorCache(final CachePolicy cachePolicy) {
		this.config = cachePolicy;
		this.storage = new ConcurrentHashMap<>(
				cachePolicy.isResultCacheEnabled() ? cachePolicy.getResultCacheSize() : 0);
		
		Context.instance().resolve(Subscription.class).apply(p -> {
			p.subscribe(this);
		});
	}

	@Override
	public void onStopping() {

		if (config.isResultCacheEnabled() && config.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.info("Saving cache to the disk: {} file. {} items to save.", config.getResultCacheFilePath(),
						storage.size());

				storage.putAll(persitence.load(config));
				persitence.store(config, storage);
				t = System.currentTimeMillis() - t;
				log.info("Saved cache to the disk: {} file. {} items was saved. Time: {}ms",
						config.getResultCacheFilePath(), storage.size(), t);

			};
			singleTaskPool.execute(task);
		}
	}

	@Override
	public void onRunning(final VehicleCapabilities vehicleCapabilities) {
		if (config.isResultCacheEnabled() && config.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.debug("Loading cache from disk", config.getResultCacheFilePath());
				storage.putAll(persitence.load(config));
				t = System.currentTimeMillis() - t;
				log.debug("Cache was load from the disk. Time: {}ms", config.getResultCacheFilePath(), t);
			};
			singleTaskPool.execute(task);
		}
	}

	boolean contains(final ConnectorResponse connectorResponse) {
		final boolean result = config.isResultCacheEnabled() && connectorResponse.isCacheable() && storage.containsKey(connectorResponse.id());
		if (log.isDebugEnabled()) {
			log.debug("Found entry in the cache: {} for: {}", result, connectorResponse.id());
		}
		return result;
	}

	Number get(final ConnectorResponse connectorResponse) {

		if (connectorResponse.isCacheable() && config.isResultCacheEnabled() && storage.containsKey(connectorResponse.id())) {
			return storage.get(connectorResponse.id());
		}

		return null;
	}

	void put(final ConnectorResponse connectorResponse, final Number result) {
		if (connectorResponse.isCacheable() && config.isResultCacheEnabled()) {
			storage.put(connectorResponse.id(), result);
		}
	}
}