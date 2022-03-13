package org.obd.metrics.codec;

import java.util.Map;
import java.util.WeakHashMap;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluatorCache {

	private boolean ENABLED = true;
	private final Map<String, Number> resultCache = new WeakHashMap<>(100000);

	boolean contains(PidDefinition pid, RawMessage rawData) {
		return ENABLED && resultCache.containsKey(toCacheKey(pid, rawData));
	}

	Number get(PidDefinition pid, RawMessage rawData) {

		final String cacheKey = toCacheKey(pid, rawData);
		if (ENABLED && resultCache.containsKey(cacheKey)) {
			return resultCache.get(cacheKey);
		}

		return null;
	}

	void put(PidDefinition pid, RawMessage rawData, Number result) {
		if (ENABLED) {
			resultCache.put(toCacheKey(pid, rawData), result);
		}
	}

	private String toCacheKey(PidDefinition pid, RawMessage raw) {
		return pid.getId() + raw.getMessage();
	}
}