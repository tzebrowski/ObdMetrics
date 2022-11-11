package org.obd.metrics.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPIDsCommand extends Command implements Codec<List<String>> {
	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);
	
	@Getter
	private final PidDefinition pid;
	
	public SupportedPIDsCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}
	
	@Override
	public List<String> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		if (log.isDebugEnabled()) {
			log.debug("PID[group:{}], processing message: {}", pid.getPid(), connectorResponse.getMessage());
		}

		if (answerCodeCodec.isAnswerCodeSuccess(pid, connectorResponse)) {

			final long encoded = answerCodeCodec.getDecimalAnswerData(pid, connectorResponse);

			final String binary = Long.toBinaryString(encoded);
			final List<String> decoded = IntStream.range(1, binary.length()).filter(i -> binary.charAt(i - 1) == '1')
					.mapToObj(i -> String.format("%02x", i)).collect(Collectors.toList());
			if (log.isDebugEnabled()) {
				log.debug("PID[group:{}] supported by ECU: [{}, {} ,{}]", pid.getPid(), encoded, binary, decoded);
			}
			return decoded;
		} else {
			log.warn("PID[group:{}], failed to transform message: {}", pid.getPid(), connectorResponse.getMessage());
			return Collections.emptyList();
		}
	}
}
