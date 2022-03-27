package org.obd.metrics.codec.formula;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;
import org.obd.metrics.raw.DecimalHandler;
import org.obd.metrics.raw.RawMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ScriptEngineParameterInjector implements DecimalHandler {

	private final List<String> FORMULA_PARAMS = IntStream.range(65, 91)
	        .boxed()
	        .map(ch -> String.valueOf((char) ch.byteValue()))
	        .collect(Collectors.toList()); // A - Z

	private final ScriptEngine scriptEngine;

	@Override
	public void handle(int j, int dec) {
		scriptEngine.put(FORMULA_PARAMS.get(j), dec);
	}

	void injectFormulaParameters(PidDefinition pidDefinition, RawMessage raw) {

		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			raw.toDecimals(pidDefinition, this);
		} else {
			scriptEngine.put("A", raw.getMessage());
		}
	}
}