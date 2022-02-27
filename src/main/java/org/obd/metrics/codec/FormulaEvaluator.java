package org.obd.metrics.codec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements Codec<Number> {

	private final AnswerCodeDecoder answerDecoder = new AnswerCodeDecoder();
	private final Decimals decimals = new Decimals();
	
	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
	        .collect(Collectors.toList()); // A - Z

	private final ScriptEngine jsEngine;

	FormulaEvaluator(String engine) {
		this.jsEngine = new ScriptEngineManager().getEngineByName(engine);
	}

	@Override
	public Number decode(PidDefinition pid, String rawData) {
		log.debug("Found PID definition: {}", pid);
		if (pid.getFormula() == null || pid.getFormula().length() == 0) {
			log.debug("No formula find in {} for: {}", pid, rawData);
		} else {
			if (answerDecoder.isAnswerCodeSuccess(pid, rawData)) {
				try {
					updateFormulaParameters(pid, rawData);
					final Object eval = jsEngine.eval(pid.getFormula());
					return TypesConverter.convert(pid, eval);

				} catch (Throwable e) {
					log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
					log.debug("Failed to evaluate the formula {}", pid.getFormula());
				}
			} else {
				log.debug("Answer code is incorrect for: {}", rawData);
			}
		}

		return null;
	}

	private void updateFormulaParameters(PidDefinition pidDefinition, String rawData) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			final String rawAnswerData = answerDecoder.getRawAnswerData(pidDefinition, rawData);
			final byte[] bytes = rawAnswerData.getBytes();

			for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
				final int decimal = decimals.toDecimal(bytes, i, 2);
				jsEngine.put(params.get(j), decimal);
			}
		} else {
			jsEngine.put(params.get(0), rawData);
		}
	}

	
}
