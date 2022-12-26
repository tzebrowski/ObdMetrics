package org.obd.metrics.codec.batch.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCommandsMapper implements BatchCommandsMapper {

	private static final byte COLON = 58;
	private static final byte[] COLON_ARR = new byte[] { COLON };

	private static final String[] DELIMETERS = new String[] { "1:", "2:", "3:", "4:", "5:" };

	private final MappingsCache cache = new MappingsCache();

	public int getCacheHit(final String query) {
		return cache.getCacheHit(query);
	}

	public Map<ObdCommand, ConnectorResponse> convert(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {
		final BatchMessageMapping mapping = findMapping(query, commands, connectorResponse);

		if (mapping == null) {
			return Collections.emptyMap();
		}

		final Map<ObdCommand, ConnectorResponse> values = new HashMap<>();

		mapping.getMappings().forEach(it -> {
			values.put(it.getCommand(), new BatchMessage(it, connectorResponse));
		});

		return values;
	}

	private BatchMessageMapping findMapping(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {
		BatchMessageMapping mapping = null;
		if (cache.contains(query)) {
			mapping = cache.lookup(query);
		} else {
			mapping = map(query, commands, connectorResponse);
			cache.insert(query, mapping);
		}
		return mapping;
	}

	private BatchMessageMapping map(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {

		final String predictedAnswerCode = commands.iterator().next().getPid().getPredictedSuccessCode();

		final int colonFirstIndexOf = connectorResponse.indexOf(COLON_ARR, 1, 0);
		final int codeIndexOf = connectorResponse.indexOf(predictedAnswerCode.getBytes(), predictedAnswerCode.length(),
				colonFirstIndexOf > 0 ? colonFirstIndexOf : 0);

		if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5
				|| (colonFirstIndexOf > 0 && (codeIndexOf - colonFirstIndexOf) == 1)) {

			final BatchMessageMapping result = new BatchMessageMapping();

			int start = codeIndexOf;

			for (final ObdCommand command : commands) {

				final PidDefinition pidDefinition = command.getPid();

				String pidId = pidDefinition.getPid();
				int pidLength = pidId.length();
				int pidIdIndexOf = connectorResponse.indexOf(pidId.getBytes(), pidLength, start);

				if (log.isDebugEnabled()) {
					log.debug("Found pid={}, indexOf={} for message={}, query={}", pidId, pidIdIndexOf,
							connectorResponse.getMessage(), query);
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
							pidIdIndexOf = connectorResponse.indexOf(pidId.getBytes(), pidLength, start);

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

				if (connectorResponse.byteAt(start) == COLON || connectorResponse.byteAt(start + 1) == COLON) {
					start += 2;
				}

				final int end = start + (pidDefinition.getLength() * 2);
				final BatchCommandMapping mapping = new BatchCommandMapping(command, start, end);
				result.getMappings().add(mapping);
				continue;
			}

			return result;
		} else {
			log.warn("Answer code for query: '{}' was not correct: {}", query, connectorResponse.getMessage());
		}
		return null;
	}
}
