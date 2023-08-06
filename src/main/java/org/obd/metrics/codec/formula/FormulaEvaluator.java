package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.codec.formula.backend.FormulaEvaluatorBackend;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements FormulaEvaluatorCodec {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache;

	FormulaEvaluator(FormulaEvaluatorConfig formulaEvaluatorConfig, final Adjustments adjustments) {
		if (formulaEvaluatorConfig == null) {
			formulaEvaluatorConfig = FormulaEvaluatorConfig.builder().build();
		}
		this.backed = FormulaEvaluatorBackend.script(formulaEvaluatorConfig);
		this.cache = new FormulaEvaluatorCache(
				adjustments == null ? CachePolicy.DEFAULT : adjustments.getCachePolicy());
	}

	@Override
	public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		if (log.isDebugEnabled()) {
			log.debug("Found PID definition: {}", pid);
		}
		if (connectorResponse.isResponseCodeSuccess(pid)) {
			if (pid.isFormulaAvailable()) {
				if (cache.contains(connectorResponse)) {
					return cache.get(connectorResponse);
				} else {
					final Number result = backed.evaluate(pid, connectorResponse);
					cache.put(connectorResponse, result);
					return result;
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("No formula found in {} for: {}", pid, connectorResponse);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Answer code is incorrect for: {}", connectorResponse.getMessage());
			}
		}
		return null;
	}
}
