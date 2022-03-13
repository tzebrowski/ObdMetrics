package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements Codec<Number> {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache = new FormulaEvaluatorCache();

	FormulaEvaluator(String engine) {
		this.backed = new FormulaEvaluatorBackend(engine);
	}

	@Override
	public Number decode(PidDefinition pid, RawMessage raw) {
		if (log.isDebugEnabled()) {
			log.debug("Found PID definition: {}", pid);
		}
		if (pid.isFormulaAvailable()) {
			if (cache.contains(pid, raw)) {
				return cache.get(pid, raw);
			} else {
				final Number result = backed.evaluate(pid, raw);
				cache.put(pid, raw, result);
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
