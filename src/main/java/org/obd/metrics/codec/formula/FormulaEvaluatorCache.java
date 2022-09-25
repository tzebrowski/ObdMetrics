package org.obd.metrics.codec.formula;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.VehicleCapabilities;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.context.Context;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorCache implements Lifecycle {

	private final CachePolicy config;
	private final Map<Long, Number> storage;
	private final FormulaEvaluatorCachePersitence persitence = new FormulaEvaluatorCachePersitence();

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	FormulaEvaluatorCache(final CachePolicy cacheConfig) {
		this.config = cacheConfig;
		this.storage = new ConcurrentHashMap<>(
				cacheConfig.isResultCacheEnabled() ? cacheConfig.getResultCacheSize() : 0);
		
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

	boolean contains(final RawMessage raw) {
		final boolean result = config.isResultCacheEnabled() && raw.isCacheable() && storage.containsKey(raw.id());
		if (log.isDebugEnabled()) {
			log.debug("Found entry in the cache: {} for: {}", result, raw.id());
		}
		return result;
	}

	Number get(final RawMessage raw) {

		if (raw.isCacheable() && config.isResultCacheEnabled() && storage.containsKey(raw.id())) {
			return storage.get(raw.id());
		}

		return null;
	}

	void put(final RawMessage raw, final Number result) {
		if (raw.isCacheable() && config.isResultCacheEnabled()) {
			storage.put(raw.id(), result);
		}
	}
}