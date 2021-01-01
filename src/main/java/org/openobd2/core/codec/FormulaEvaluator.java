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
	public static FormulaEvaluator build(@NonNull PidRegistry pids) {
		return new FormulaEvaluator(pids);
	}

	@Override
	public Object decode(@NonNull String rawData) {
		return decode(rawData, Object.class);
	}

	Object decode(@NonNull String rawData, @NonNull Class<Object> clazz) {

		final PidDefinition pid = pidRegistry.findByAnswerRawData(rawData);

		if (null == pid) {
			log.debug("No PID definition found for: {}", rawData);
		} else {
			log.debug("Found PID definition: {}", pid);
			if (pid.getFormula() == null || pid.getFormula().length() == 0) {
				log.debug("No formula find in {} for: {}", pid, rawData);
			} else {
				if (decoder.isSuccessAnswerCode(pid, rawData)) {

					final String rawAnswerData = decoder.getRawAnswerData(pid, rawData);
					for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
						final String hexValue = rawAnswerData.substring(i, i + 2);
						jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
					}

					try {

						long time = System.currentTimeMillis();
						final Object eval = jsEngine.eval(pid.getFormula());
						time = System.currentTimeMillis() - time;
						log.debug("Execution time: {}ms", time);
						return clazz.cast(eval);
					} catch (ScriptException e) {
						log.error("Failed to evaluate the formula {}", pid.getFormula());
					}
				} else {
					log.warn("Answer code is not success for: {}", rawData);
				}
			}
		}
		return null;
	}
}
