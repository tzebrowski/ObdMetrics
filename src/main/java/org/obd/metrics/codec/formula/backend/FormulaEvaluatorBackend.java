package org.obd.metrics.codec.formula.backend;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

public interface FormulaEvaluatorBackend {

	Number evaluate(PidDefinition pid, ConnectorResponse raw);

	public static FormulaEvaluatorBackend script(FormulaEvaluatorConfig formulaEvaluatorConfig) {
		return new ScriptEngineBackend(formulaEvaluatorConfig);
	}
}