package org.openobd2.core.converter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidDefinitionRegistry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluator implements Converter<Object> {

	private static final int SUCCCESS_CODE = 40;

	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
			.collect(Collectors.toList()); // A - Z

	private static final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");

	private final PidDefinitionRegistry definitionsRegistry;

	@Builder
	public static FormulaEvaluator build(@NonNull PidDefinitionRegistry definitionsRegistry) {

		return  new FormulaEvaluator(definitionsRegistry);
	}

	@Override
	public Object convert(@NonNull String rawData) {
		return convert(rawData, Object.class);
	}

	public <T> T convert(@NonNull String rawData, @NonNull Class<T> clazz) {

		final PidDefinition pidDefinition = definitionsRegistry.findByAnswerRawData(rawData);

		if (null == pidDefinition) {
			log.debug("No definition found for: {}", rawData);
		} else {
			log.debug("Found definition: {}", pidDefinition);
			if (isSuccessAnswerCode(rawData, pidDefinition)) {

				final String rawAnswerData = getRawAnswerData(rawData, pidDefinition);
				for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
					final String hexValue = rawAnswerData.substring(i, i + 2);
					jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
				}

				try {

					long time = System.currentTimeMillis();
					Object eval = jsEngine.eval(pidDefinition.getFormula());
					time = System.currentTimeMillis() - time;
					log.debug("Execution time: {}ms", time);
					return clazz.cast(eval);
				} catch (ScriptException e) {
					log.error("Failed to evaluate rule");
				}
			} else {
				log.warn("Answer code is not success for: {}", rawData);
			}
		}
		return null;
	}

	private boolean isSuccessAnswerCode(String raw, PidDefinition rule) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode(rule));
	}

	private String getPredictedAnswerCode(PidDefinition rule) {
		// success code = 0x40 + mode + pid
		return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(rule.getMode())) + rule.getPid()).toLowerCase();
	}

	private String getRawAnswerData(String raw, PidDefinition rule) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode(rule).length());
	}
}
