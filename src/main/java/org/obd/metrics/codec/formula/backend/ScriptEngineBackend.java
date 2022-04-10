package org.obd.metrics.codec.formula.backend;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ScriptEngineBackend implements FormulaEvaluatorBackend {

	private final ScriptEngine scriptEngine;

	private final ScriptEngineParameterInjector engineParameterInjector;

	ScriptEngineBackend(String engine) {
		this.scriptEngine = new ScriptEngineManager().getEngineByName(engine);
		this.engineParameterInjector = new ScriptEngineParameterInjector(this.scriptEngine);
	}

	@Override
	public Number evaluate(PidDefinition pid, RawMessage raw) {

		try {
			engineParameterInjector.injectFormulaParameters(pid, raw);
			final Object eval = scriptEngine.eval(pid.getFormula());
			return TypesConverter.convert(pid, eval);
		} catch (Throwable e) {
			log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
			log.debug("Failed to evaluate the formula {}", pid.getFormula());
		}
		return null;
	}
}
