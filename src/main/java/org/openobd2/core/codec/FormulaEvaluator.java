package org.openobd2.core.codec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluator implements Codec<Object> {

	private final CommandReplyDecoder decoder = new CommandReplyDecoder();

	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
			.collect(Collectors.toList()); // A - Z

	private static final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");

	private final PidRegistry pidRegistry;

	@Builder
	public static FormulaEvaluator build(@NonNull PidRegistry definitionsRegistry) {
		return new FormulaEvaluator(definitionsRegistry);
	}

	@Override
	public Object decode(@NonNull String rawData) {
		return convert(rawData, Object.class);
	}

	public <T> T convert(@NonNull String rawData, @NonNull Class<T> clazz) {

		final PidDefinition pidDefinition = pidRegistry.findByAnswerRawData(rawData);

		if (null == pidDefinition) {
			log.debug("No definition found for: {}", rawData);
		} else {
			log.debug("Found definition: {}", pidDefinition);
			if (pidDefinition.getFormula() == null || pidDefinition.getFormula().length() == 0) {
				log.debug("No formula find in {} for: {}", pidDefinition, rawData);
			} else {
				if (decoder.isSuccessAnswerCode(pidDefinition, rawData)) {

					final String rawAnswerData = decoder.getRawAnswerData(pidDefinition, rawData);
					for (int i = 0, j = 0; i < pidDefinition.getLength() * 2; i += 2, j++) {
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
						log.error("Failed to evaluate the formula {}", pidDefinition.getFormula());
					}
				} else {
					log.warn("Answer code is not success for: {}", rawData);
				}
			}
		}
		return null;
	}

}
