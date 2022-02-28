package org.obd.metrics.codec;

import java.util.Map;
import java.util.WeakHashMap;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluatorCache {

	private boolean ENABLE_CACHE = true;
	private final Map<String, Number> resultCache = new WeakHashMap<>(100000);

	boolean contains(PidDefinition pid, String rawData) {
		return ENABLE_CACHE && resultCache.containsKey(getCacheKey(pid, rawData));
	}

	Number get(PidDefinition pid, String rawData) {

		final String cacheKey = getCacheKey(pid, rawData);
		if (ENABLE_CACHE && resultCache.containsKey(cacheKey)) {
			return resultCache.get(cacheKey);
		}

		return null;
	}

	void put(PidDefinition pid, String rawData, Number result) {

		final String cacheKey = getCacheKey(pid, rawData);

		if (ENABLE_CACHE) {
			resultCache.put(cacheKey, result);
		}
	}

	private String getCacheKey(PidDefinition pid, String rawData) {
		return pid.getId() + rawData;
	}
}