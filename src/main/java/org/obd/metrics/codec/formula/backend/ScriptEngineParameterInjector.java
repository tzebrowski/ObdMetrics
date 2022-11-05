package org.obd.metrics.codec.formula.backend;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.DecimalReceiver;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ScriptEngineParameterInjector implements DecimalReceiver {

	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	private final List<String> FORMULA_PARAMS = IntStream.range(65, 91).boxed()
			.map(ch -> String.valueOf((char) ch.byteValue())).collect(Collectors.toList()); // A - Z

	private final ScriptEngine scriptEngine;

	@Override
	public void receive(final int j, final int dec) {
		scriptEngine.put(FORMULA_PARAMS.get(j), dec);
	}

	void injectFormulaParameters(final PidDefinition pidDefinition, final ConnectorMessage raw) {

		scriptEngine.put("DEBUG_PARAMS", formulaEvaluatorConfig.getDebug());

		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			raw.exctractDecimals(pidDefinition, this);
		} else {
			scriptEngine.put("A", raw.getMessage());
		}
	}
}