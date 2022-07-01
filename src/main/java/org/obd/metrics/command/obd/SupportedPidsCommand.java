package org.obd.metrics.command.obd;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPidsCommand extends ObdCommand implements Codec<List<String>> {
	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);

	public SupportedPidsCommand(final long id, final String pid) {
		super(new PidDefinition(id, 0, "", "01", pid, "", "Supported PIDs", 0, 0, PidDefinition.ValueType.DOUBLE));
	}

	@Override
	public List<String> decode(final PidDefinition pid, final RawMessage raw) {

		if (log.isDebugEnabled()) {
			log.debug("PID[group:{}], processing message: {}", pid.getPid(), raw.getMessage());
		}

		if (answerCodeCodec.isAnswerCodeSuccess(pid, raw)) {

			final long encoded = answerCodeCodec.getDecimalAnswerData(pid, raw);

			final String binary = Long.toBinaryString(encoded);
			final List<String> decoded = IntStream.range(1, binary.length()).filter(i -> binary.charAt(i - 1) == '1')
					.mapToObj(i -> String.format("%02x", i)).collect(Collectors.toList());
			if (log.isDebugEnabled()) {
				log.debug("PID[group:{}] supported by ECU: [{}, {} ,{}]", pid.getPid(), encoded, binary, decoded);
			}
			return decoded;
		} else {
			log.warn("PID[group:{}], failed to transform message: {}", pid.getPid(), raw.getMessage());
			return Collections.emptyList();
		}
	}
}
