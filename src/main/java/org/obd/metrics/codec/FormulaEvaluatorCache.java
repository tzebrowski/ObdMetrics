package org.obd.metrics.codec;

import java.util.Map;
import java.util.WeakHashMap;

import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.raw.RawMessage;

final class FormulaEvaluatorCache {

	private final CacheConfig cacheConfig;
	private final Map<Long, Number> resultCache;

	FormulaEvaluatorCache(CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
		this.resultCache = new WeakHashMap<>(cacheConfig.isResultCacheEnabled() ? cacheConfig.getResultCacheSize() : 0);
	}

	boolean contains(RawMessage raw) {
		return cacheConfig.isResultCacheEnabled() && resultCache.containsKey(raw.id());
	}

	Number get(RawMessage raw) {

		final Long cacheKey = raw.id();
		if (cacheConfig.isResultCacheEnabled() && resultCache.containsKey(cacheKey)) {
			return resultCache.get(cacheKey);
		}

		return null;
	}

	void put(RawMessage raw, Number result) {
		if (cacheConfig.isResultCacheEnabled()) {
			resultCache.put(raw.id(), result);
		}
	}
}