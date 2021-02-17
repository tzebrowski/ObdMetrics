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

	@Override
	public Number decode(@NonNull PidDefinition pid, @NonNull String rawData) {
		Number decode = codec.decode(pid, rawData);
		if (null == decode) {
			return decode;
		} else {
			return generate(pid, decode);
		}
	}

	private Number generate(PidDefinition pid, Number value) {
		var increment = generatorData.get(pid);
		if (increment == null) {
			increment = 0.0;
		}
		increment += 5;
		generatorData.put(pid, increment);
		return value.doubleValue() + increment;
	}
}
