package org.obd.metrics.codec.formula;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorBackend {

	private final ScriptEngine scriptEngine;

	final ScriptEngineParameterInjector engineParameterInjector;

	FormulaEvaluatorBackend(String engine) {
		this.scriptEngine = new ScriptEngineManager().getEngineByName(engine);
		this.engineParameterInjector = new ScriptEngineParameterInjector(this.scriptEngine);
	}

	Number evaluate(PidDefinition pid, RawMessage raw) {

		if (AnswerCodeCodec.instance.isAnswerCodeSuccess(pid, raw)) {
			try {
				engineParameterInjector.injectFormulaParameters(pid, raw);
				final Object eval = scriptEngine.eval(pid.getFormula());
				return TypesConverter.convert(pid, eval);
			} catch (Throwable e) {
				log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
				log.debug("Failed to evaluate the formula {}", pid.getFormula());
			}
		} else {
			log.debug("Answer code is incorrect for: {}", raw.getMessage());
		}
		return null;
	}
}
