package org.obd.metrics.command.obd;

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
		super(new PidDefinition(100001l, 0, "", "01", pid, "", "Supported PIDs", 0, 0, PidDefinition.Type.DOUBLE));
	}

	@Override
	public List<String> decode(final PidDefinition pid, final String data) {
		var decoder = new MetricsDecoder();

		if (decoder.isSuccessAnswerCode(pid, data)) {
			var decimalAnswerData = decoder.getDecimalAnswerData(pid, data);
			var binStr = Long.toBinaryString(decimalAnswerData);
			var decode = IntStream.range(1, binStr.length())
			        .filter(i -> binStr.charAt(i - 1) == '1')
			        .mapToObj(i -> String.format("%02x", i))
			        .collect(Collectors.toList());

			log.debug(" {}  --> {} --> {}", decimalAnswerData, binStr, decode);
			return decode;
		} else {
			log.debug("Failed to transform data: {}", data);
			return List.of();
		}
	}
}
