package org.obd.metrics.command.obd;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPidsCommand extends ObdCommand implements Codec<List<String>> {

	public SupportedPidsCommand(String pid) {
		super(new PidDefinition(100001l, 0, "", "01", pid, "", "Supported PIDs", 0, 0, PidDefinition.ValueType.DOUBLE));
	}

	@Override
	public List<String> decode(final PidDefinition pid, final String data) {
		final MetricsDecoder decoder = new MetricsDecoder();

		if (decoder.isSuccessAnswerCode(pid, data)) {
			final long encoded = decoder.getDecimalAnswerData(pid, data);
			final String binary = Long.toBinaryString(encoded);
			final List<String> decoded = IntStream.range(1, binary.length())
			        .filter(i -> binary.charAt(i - 1) == '1')
			        .mapToObj(i -> String.format("%02x", i))
			        .collect(Collectors.toList());

			log.info("Pids supported by ECU: [{}, {} ,{}]", encoded, binary, decoded);
			return decoded;
		} else {
			log.debug("Failed to transform data: {}", data);
			return Arrays.asList();
		}
	}
}
