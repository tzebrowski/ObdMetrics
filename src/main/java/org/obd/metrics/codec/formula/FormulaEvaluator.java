package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.formula.backend.FormulaEvaluatorBackend;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements FormulaEvaluatorCodec {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache;
	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(true);

	FormulaEvaluator(FormulaEvaluatorConfig formulaEvaluatorConfig, final Adjustments adjustments) {
		if (formulaEvaluatorConfig == null) {
			formulaEvaluatorConfig = FormulaEvaluatorConfig.builder().build();
		}
		this.backed = FormulaEvaluatorBackend.script(formulaEvaluatorConfig);
		this.cache = new FormulaEvaluatorCache(
				adjustments == null ? CachePolicy.DEFAULT : adjustments.getCacheConfig());
	}

	@Override
	public Number decode(final PidDefinition pid, final ConnectorMessage raw) {
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
