package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Generator implements Codec<Number> {

	private final Map<PidDefinition, Double> generatorData = new HashMap<>();
	private final Codec<Number> codec;
	private final GeneratorPolicy generatorPolicy;

	@Override
	public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		final Number decode = codec.decode(pid, connectorResponse);
		if (null == decode) {
			return decode;
		} else {
			return generate(pid, decode);
		}
	}

	private Number generate(final PidDefinition pid, final Number value) {
		Double current = generatorData.get(pid);
		if (current == null) {
			current = pid.getMin().doubleValue();
		}

		if (pid.getMax() == null) {
			current += generatorPolicy.getIncrement();
		} else {
			current = calculate(current, pid.getMax().longValue());
			if (current >= pid.getMax().doubleValue()) {
				current = pid.getMin().doubleValue();
			}		
		}
		generatorData.put(pid, current);
		return current;
	}

	private Double calculate(final double currentValue, final long maxValue) {
		double current = currentValue;

		if (maxValue < 2) {
			current += 0.005;
		} else if (maxValue < 5) {
			current += 0.05;
		} else if (maxValue <= 21 && maxValue >= 5) {
			current += 0.1;
		} else if (maxValue <= 100 && maxValue >= 22) {
			current += 1;
		} else if (maxValue <= 200 && maxValue >= 100) {
			current += 2;
		} else if (maxValue >= 1000) {
			current += 10;
		} else {
			current += 10;
		}
		return current;
	}
}
