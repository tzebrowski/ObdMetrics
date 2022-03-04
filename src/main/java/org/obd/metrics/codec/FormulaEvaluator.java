package org.obd.metrics.codec;

import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinition;

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
		if (pid.getFormula() == null || pid.getFormula().length() == 0) {
			if(log.isDebugEnabled()) {
				log.debug("No formula found in {} for: {}", pid, raw);
			}
		} else {
			if (cache.contains(pid, raw)) {
				return cache.get(pid, raw);
			} else {
				final Number result = backed.evaluate(pid, raw);
				cache.put(pid, raw, result);
				return result;
			}
		}
		return null;
	}
}
