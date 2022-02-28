package org.obd.metrics.codec;

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
	public Number decode(PidDefinition pid, String rawData) {

		log.debug("Found PID definition: {}", pid);

		if (pid.getFormula() == null || pid.getFormula().length() == 0) {
			log.debug("No formula found in {} for: {}", pid, rawData);
		} else {
			if (cache.contains(pid, rawData)) {
				return cache.get(pid, rawData);
			} else {
				final Number result = backed.evaluate(pid, rawData);
				cache.put(pid, rawData, result);
				return result;
			}
		}

		return null;
	}
}
