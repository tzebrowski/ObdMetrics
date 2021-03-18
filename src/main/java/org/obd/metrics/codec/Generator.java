package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Generator implements Codec<Number> {

	private final Map<PidDefinition, Double> generatorData = new HashMap<>();
	private final Codec<Number> codec;
	private final GeneratorSpec generatorSpec;

	@Override
	public Number decode(PidDefinition pid, String rawData) {
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
			current += generatorSpec.getIncrement();
		} else {
			if (current < pid.getMax().longValue()) {
				if (generatorSpec.isSmart()) {
					if (pid.getMax().longValue() < 5) {
						current += 0.05;
					} else if (pid.getMax().longValue() <= 20 && pid.getMax().longValue() >= 5) {
						current += 1;
					} else if (pid.getMax().longValue() <= 100 && pid.getMax().longValue() >= 20) {
						current += 2;
					} else if (pid.getMax().longValue() <= 200 && pid.getMax().longValue() >= 100) {
						current += 4;
					} else {
						current += 10;
					}

				} else {
					current += generatorSpec.getIncrement();
				}
			}
		}

		generatorData.put(pid, current);
		return value.doubleValue() + current;
	}
}
