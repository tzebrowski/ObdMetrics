package org.obd.metrics.codec.formula;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.DeviceProperties;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorCache implements Lifecycle {

	private final CacheConfig cacheConfig;
	private Map<Long, Number> items;
	private CachePersitence cachePersitence = new CachePersitence();

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	FormulaEvaluatorCache(CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
		this.items = new ConcurrentHashMap<>(cacheConfig.isResultCacheEnabled() ? cacheConfig.getResultCacheSize() : 0);
		Lifecycle.subscription.subscribe(this);
	}

	@Override
	public void onStopped() {

		if (cacheConfig.isResultCacheEnabled() && cacheConfig.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.info("Saving cache to the disk: {} file. {} items to save.",
				        cacheConfig.getResultCacheFilePath(), items.size());

				items.putAll(cachePersitence.load(cacheConfig));
				cachePersitence.store(cacheConfig, items);
				t = System.currentTimeMillis() - t;
				log.info("Saved cache to the disk: {} file. {} items was saved. Time: {}ms",
				        cacheConfig.getResultCacheFilePath(), items.size(), t);

			};
			singleTaskPool.execute(task);
		}
	}

	@Override
	public void onRunning(DeviceProperties properties) {
		if (cacheConfig.isResultCacheEnabled() && cacheConfig.isStoreResultCacheOnDisk()) {
			final Runnable task = () -> {
				long t = System.currentTimeMillis();
				log.debug("Loading cache from disk", cacheConfig.getResultCacheFilePath());
				items.putAll(cachePersitence.load(cacheConfig));
				t = System.currentTimeMillis() - t;
				log.debug("Cache was loadfrom disk. Time: {}ms", cacheConfig.getResultCacheFilePath(), t);
			};
			singleTaskPool.execute(task);
		}
	}

	boolean contains(RawMessage raw) {
		final boolean result = cacheConfig.isResultCacheEnabled() && raw.isCacheable() && items.containsKey(raw.id());
		if (log.isDebugEnabled()) {
			log.debug("Found entry in the cache: {} for: {}", result, raw.id());
		}
		return result;
	}

	Number get(RawMessage raw) {

		if (raw.isCacheable() && cacheConfig.isResultCacheEnabled() && items.containsKey(raw.id())) {
			return items.get(raw.id());
		}

		return null;
	}

	void put(RawMessage raw, Number result) {
		if (raw.isCacheable() && cacheConfig.isResultCacheEnabled()) {
			items.put(raw.id(), result);
		}
	}
}