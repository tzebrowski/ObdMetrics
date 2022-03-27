package org.obd.metrics.codec.formula;

import org.obd.metrics.api.Adjustments;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FormulaEvaluator implements Codec<Number> {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache;

	public FormulaEvaluator(String engine, Adjustments adjustments) {
		this.backed = new FormulaEvaluatorBackend(engine);
		this.cache = new FormulaEvaluatorCache(
		        adjustments == null ? CacheConfig.DEFAULT : adjustments.getCacheConfig());
	}

	@Override
	public Number decode(PidDefinition pid, RawMessage raw) {
		if (log.isDebugEnabled()) {
			log.debug("Found PID definition: {}", pid);
		}
		if (pid.isFormulaAvailable()) {
			if (cache.contains(raw)) {
				return cache.get(raw);
			} else {
				final Number result = backed.evaluate(pid, raw);
				cache.put(raw, result);
				return result;
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("No formula found in {} for: {}", pid, raw);
			}
			return null;
		}
	}
}
