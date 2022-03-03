package org.obd.metrics.codec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorBackend {

	private static final List<String> PARAMS = IntStream.range(65, 91)
	        .boxed()
	        .map(ch -> String.valueOf((char) ch.byteValue()))
	        .collect(Collectors.toList()); // A - Z

	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec();
	private final Decimals decimals = new Decimals();
	private final ScriptEngine scriptEngine;

	FormulaEvaluatorBackend(String engine) {
		this.scriptEngine = new ScriptEngineManager().getEngineByName(engine);
	}

	Number evaluate(PidDefinition pid, RawMessage raw) {
		String rawData = raw.getMessage();
		
		if (answerCodeCodec.isAnswerCodeSuccess(pid, rawData)) {
			try {
				updateFormulaParameters(pid, rawData);
				final Object eval = scriptEngine.eval(pid.getFormula());
				
				return TypesConverter.convert(pid, eval);
			} catch (Throwable e) {
				log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
				log.debug("Failed to evaluate the formula {}", pid.getFormula());
			}
		} else {
			log.debug("Answer code is incorrect for: {}", rawData);
		}
		return null;
	}

	private void updateFormulaParameters(PidDefinition pidDefinition, String rawData) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			final int rawDataStart  = answerCodeCodec.getSuccessAnswerCodeLength(pidDefinition);
			final byte[] bytes = rawData.getBytes();

			for (int pos = rawDataStart, j = 0; pos < rawData.length(); pos += 2, j++) {
				final int decimal = decimals.twoBytesToDecimal(bytes, pos);
				scriptEngine.put(PARAMS.get(j), decimal);
			}
		} else {
			scriptEngine.put(PARAMS.get(0), rawData);
		}
	}
}
