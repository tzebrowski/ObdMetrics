package org.obd.metrics.codec.formula.backend;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ScriptEngineBackend implements FormulaEvaluatorBackend {

	private final ScriptEngine scriptEngine;

	private final ScriptEngineParameterInjector engineParameterInjector;

	ScriptEngineBackend(FormulaEvaluatorConfig formulaEvaluatorConfig) {
		log.info("Creating formula evaluator for {}", formulaEvaluatorConfig);
		this.scriptEngine = new ScriptEngineManager().getEngineByName(formulaEvaluatorConfig.getScriptEngine());
		this.engineParameterInjector = new ScriptEngineParameterInjector(formulaEvaluatorConfig, scriptEngine);
	}

	@Override
	public Number evaluate(final PidDefinition pid, final ConnectorResponse raw) {

		try {
			engineParameterInjector.injectFormulaParameters(pid, raw);
			final Object eval = scriptEngine.eval(pid.getFormula());
			return TypesConverter.convert(pid, eval);
		} catch (final Throwable e) {
			if (log.isTraceEnabled()){
				log.trace("Failed to evaluate the formula {} for PID: {}, message: {}", pid.getFormula(), pid.getPid(),new String(raw.getBytes()), e);
			}
			
			log.error("Failed to evaluate the formula {} for PID: {}", pid.getFormula(), pid.getPid());
		}
		return null;
	}
}
