package org.obd.metrics.codec;

import java.util.Map;
import java.util.WeakHashMap;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluatorCache {

	private boolean ENABLED = true;
	private final Map<String, Number> resultCache = new WeakHashMap<>(100000);
	
	
	
	boolean contains(PidDefinition pid, String rawData) {
		return ENABLED && resultCache.containsKey(toCacheKey(pid, rawData));
	}

	Number get(PidDefinition pid, String rawData) {

		final String cacheKey = toCacheKey(pid, rawData);
		if (ENABLED && resultCache.containsKey(cacheKey)) {
			return resultCache.get(cacheKey);
		}

		return null;
	}

	void put(PidDefinition pid, String rawData, Number result) {

		final String cacheKey = toCacheKey(pid, rawData);

		if (ENABLED) {
			resultCache.put(cacheKey, result);
		}
	}

	private String toCacheKey(PidDefinition pid, String rawData) {
		return pid.getId() + rawData;
	}
}