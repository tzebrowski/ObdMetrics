package org.obd.metrics.codec.formula;

import java.util.HashMap;
import java.util.Map;

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

	FormulaEvaluatorCache(CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
		this.items = new HashMap<>(cacheConfig.isResultCacheEnabled() ? cacheConfig.getResultCacheSize() : 0);
		Lifecycle.subscription.subscribe(this);
	}

	@Override
	public void onStopped() {

		if (cacheConfig.isResultCacheEnabled() && cacheConfig.isStoreResultCacheOnDisk()) {
			log.info("Saving cache to the disk: {} file. {} items to save.",
			        cacheConfig.getResultCacheFilePath(), items.size());

			items.putAll(cachePersitence.load(cacheConfig));
			cachePersitence.store(cacheConfig, items);
			log.info("Saved cache to the disk: {} file. {} items was saved.",
			        cacheConfig.getResultCacheFilePath(), items.size());
		}
	}

	@Override
	public void onRunning(DeviceProperties properties) {
		if (cacheConfig.isResultCacheEnabled() && cacheConfig.isStoreResultCacheOnDisk()) {
			log.debug("Loading cache from disk", cacheConfig.getResultCacheFilePath());
			this.items.putAll(cachePersitence.load(cacheConfig));
		}
	}

	boolean contains(RawMessage raw) {
		return cacheConfig.isResultCacheEnabled() && raw.isCacheable() && items.containsKey(raw.id());
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