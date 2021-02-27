package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
final class Generator implements Codec<Number> {

	private final Map<PidDefinition, Double> generatorData = new HashMap<>();
	private final Codec<Number> codec;
	private final double increment;

	@Override
	public Number decode(@NonNull PidDefinition pid, @NonNull String rawData) {
		var decode = codec.decode(pid, rawData);
		if (null == decode) {
			return decode;
		} else {
			return generate(pid, decode);
		}
	}

	private Number generate(PidDefinition pid, Number value) {
		var current = generatorData.get(pid);
		if (current == null) {
			current = 0.0;
		}

		if (pid.getMax() == null) {
			current += increment;
		} else {
			if (current < pid.getMax().longValue()) {
				current += increment;
			} else {

			}
		}

		generatorData.put(pid, current);
		return value.doubleValue() + current;
	}
}
