package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchResponseMapper {

	protected final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);
	
	private static final String[] DELIMETERS = new String[] {"1:","2:","3:","4:","5:"};
	
	BatchResponseMapping map(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {
		
		final String predictedAnswerCode = answerCodeCodec
				.getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
		
		final byte[] message = connectorResponse.getBytes();
		
		final int colonFirstIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), ":".getBytes(), 1, 0);
		final int codeIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), predictedAnswerCode.getBytes(),
				predictedAnswerCode.length(), colonFirstIndexOf > 0 ? colonFirstIndexOf : 0);

		if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5
				|| (colonFirstIndexOf > 0 && (codeIndexOf - colonFirstIndexOf) == 1)) {

			final BatchResponseMapping batchResponseMapping = new BatchResponseMapping();

			int start = codeIndexOf;

			for (final ObdCommand command : commands) {

				final PidDefinition pidDefinition = command.getPid();

				String pidId = pidDefinition.getPid();
				int pidLength = pidId.length();
				int pidIdIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), pidId.getBytes(), pidLength,
						start);

				if (log.isDebugEnabled()) {
					log.debug("Found pid={}, indexOf={} for message={}, query={}", pidId, pidIdIndexOf,
							new String(message), query);
				}

				if (pidIdIndexOf == -1) {
					final int length = pidLength;
					final String id = pidId;
					for (final String delim : DELIMETERS) {
						pidLength = length;
						pidId = id;

						if (pidLength == 4) {
							pidId = pidId.substring(0, 2) + delim + pidId.substring(2, 4);
							pidLength = pidId.length();
							pidIdIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), pidId.getBytes(),
									pidLength, start);

							if (log.isDebugEnabled()) {
								log.debug("Another iteration. Found pid={}, indexOf={}", pidId, pidIdIndexOf);
							}
						}
						if (pidIdIndexOf == -1) {
							continue;
						} else {
							break;
						}
					}

					if (pidIdIndexOf == -1) {
						continue;
					}
				}

				start = pidIdIndexOf + pidLength;

				if ((char) message[start] == ':' || (char) message[start + 1] == ':') {
					start += 2;
				}

				final int end = start + (pidDefinition.getLength() * 2);
				final BatchResponsePIDMapping pidMapping = new BatchResponsePIDMapping(command,
						start, end);
				batchResponseMapping.getMappings().add(pidMapping);
				continue;

			}
			
			return batchResponseMapping;
		} else {
			log.warn("Answer code for query: '{}' was not correct: {}", query, connectorResponse.getMessage());
		}
		return null;
	}
}
