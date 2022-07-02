package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CacheConfig;
import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.formula.backend.FormulaEvaluatorBackend;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements FormulaEvaluatorCodec {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache;
	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(true);

	FormulaEvaluator(final String engine, final Adjustments adjustments) {
		this.backed = FormulaEvaluatorBackend.script(engine);
		this.cache = new FormulaEvaluatorCache(
				adjustments == null ? CacheConfig.DEFAULT : adjustments.getCacheConfig());
	}

	@Override
	public Number decode(final PidDefinition pid, final RawMessage raw) {
		if (log.isDebugEnabled()) {
			log.debug("Found PID definition: {}", pid);
		}
		if (answerCodeCodec.isAnswerCodeSuccess(pid, raw)) {
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
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Answer code is incorrect for: {}", raw.getMessage());
			}
		}
		return null;
	}
}
