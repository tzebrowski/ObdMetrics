package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

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

	private final ScriptEngine jsEngine;

	private final PidRegistry pidRegistry;
	private boolean simulatorEnabled = false;
	final Map<PidDefinition, Double> simulatorData = new HashMap<>();

	@Builder
	public static FormulaEvaluator build(@NonNull PidRegistry pids, @NonNull String engine,boolean simulatorEnabled) {
		return new FormulaEvaluator(new ScriptEngineManager().getEngineByName(engine), pids,  simulatorEnabled);
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
					try {
						final String rawAnswerData = decoder.getRawAnswerData(pid, rawData);
						for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
							final String hexValue = rawAnswerData.substring(i, i + 2);
							jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
						}

						long time = System.currentTimeMillis();
						final Object eval = jsEngine.eval(pid.getFormula());
						time = System.currentTimeMillis() - time;
						log.debug("Execution time: {}ms", time);

						final Number value = (Number) clazz.cast(eval);
						if (simulatorEnabled) {
							Double increment = simulatorData.get(pid);
							if (increment == null) {
								increment = 0.0;
							}
							increment += 5;
							simulatorData.put(pid, increment);
							return value.doubleValue() + increment;
						}else {
							return value;
						}
					} catch (Throwable e) {
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
