package org.obd.metrics.codec;

import java.util.Map;
import java.util.WeakHashMap;

import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

final class FormulaEvaluatorCache {

	private final CacheConfig cacheConfig;
	private final Map<String, Number> resultCache;

	FormulaEvaluatorCache(CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
		this.resultCache = new WeakHashMap<>(cacheConfig.isResultCacheEnabled() ? cacheConfig.getResultCacheSize() : 0);
	}

	boolean contains(PidDefinition pid, RawMessage rawData) {
		return cacheConfig.isResultCacheEnabled() && resultCache.containsKey(toCacheKey(pid, rawData));
	}

	Number get(PidDefinition pid, RawMessage rawData) {

		final String cacheKey = toCacheKey(pid, rawData);
		if (cacheConfig.isResultCacheEnabled() && resultCache.containsKey(cacheKey)) {
			return resultCache.get(cacheKey);
		}

		return null;
	}

	void put(PidDefinition pid, RawMessage rawData, Number result) {
		if (cacheConfig.isResultCacheEnabled()) {
			resultCache.put(toCacheKey(pid, rawData), result);
		}
	}

	private String toCacheKey(PidDefinition pid, RawMessage raw) {
		return pid.getId() + raw.getMessage();
	}
}