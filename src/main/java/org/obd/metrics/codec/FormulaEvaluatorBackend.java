package org.obd.metrics.codec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluatorBackend {

	private static final Map<Integer, Integer> DECIMALS_CACHE = new WeakHashMap<>(100000);
	private static final List<String> PARAMS = IntStream.range(65, 91)
			.boxed()
			.map(ch -> String.valueOf((char) ch.byteValue()))
	        .collect(Collectors.toList()); // A - Z

	
	private final AnswerCodeDecoder answerDecoder = new AnswerCodeDecoder();
	private final Decimals decimals = new Decimals();
	private final ScriptEngine jsEngine;

	FormulaEvaluatorBackend(String engine) {
		this.jsEngine = new ScriptEngineManager().getEngineByName(engine);
	}

	Number evaluate(PidDefinition pid, String rawData) {
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
		return null;
	}

	private void updateFormulaParameters(PidDefinition pidDefinition, String rawData) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			final String rawAnswerData = answerDecoder.getRawAnswerData(pidDefinition, rawData);
			final byte[] bytes = rawAnswerData.getBytes();

			for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
				final int decimal = getDecimal(bytes, i);
				jsEngine.put(PARAMS.get(j), decimal);
			}
		} else {
			jsEngine.put(PARAMS.get(0), rawData);
		}
	}

	private int getDecimal(final byte[] bytes, int i) {
		final byte[] range = Arrays.copyOfRange(bytes, i, i + 2);
		final int hashCode = Arrays.hashCode(range);
		if (DECIMALS_CACHE.containsKey(hashCode)) {
			return DECIMALS_CACHE.get(hashCode);
		} else {
			int decimal = decimals.toDecimal(range);
			DECIMALS_CACHE.put(hashCode, decimal);
			return decimal;
		}
	}
}
