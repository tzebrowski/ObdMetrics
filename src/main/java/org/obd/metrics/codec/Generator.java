package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Generator implements Codec<Number> {

	private final Map<PidDefinition, Double> generatorData = new HashMap<>();
	private final Codec<Number> codec;
	private final GeneratorSpec generatorSpec;

	@Override
	public Number decode(PidDefinition pid, RawMessage rawData) {
		final Number decode = codec.decode(pid, rawData);
		if (null == decode) {
			return decode;
		} else {
			return generate(pid, decode);
		}
	}

	private Number generate(PidDefinition pid, Number value) {
		Double current = generatorData.get(pid);
		if (current == null) {
			current = 0.0;
		}

		if (pid.getMax() == null) {
			current += generatorSpec.getIncrement();
		} else {
			final long maxValue = pid.getMax().longValue();
			if (value.doubleValue() + current < maxValue) {
				if (generatorSpec.isSmart()) {
					current = calculate(current, maxValue);
				} else {
					current += generatorSpec.getIncrement();
				}
			} else {
				current = pid.getMin().doubleValue();
			}
		}
		generatorData.put(pid, current);
		return value.doubleValue() + current;
	}

	private Double calculate(final Double currentValue, final long maxValue) {
		Double current = currentValue;

		if (maxValue < 2) {
			current += 0.005;
		} else if (maxValue < 5) {
			current += 0.05;
		} else if (maxValue <= 20 && maxValue >= 5) {
			current += 1;
		} else if (maxValue <= 100 && maxValue >= 20) {
			current += 2;
		} else if (maxValue <= 200 && maxValue >= 100) {
			current += 4;
		} else if (maxValue >= 1000) {
			current += 20;
		} else {
			current += 10;
		}
		return current;
	}
}
