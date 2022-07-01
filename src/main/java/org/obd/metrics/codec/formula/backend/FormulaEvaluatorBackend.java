package org.obd.metrics.codec.formula.backend;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

public interface FormulaEvaluatorBackend {

	Number evaluate(PidDefinition pid, RawMessage raw);

	public static FormulaEvaluatorBackend script(final String engine) {
		return new ScriptEngineBackend(engine);
	}
}