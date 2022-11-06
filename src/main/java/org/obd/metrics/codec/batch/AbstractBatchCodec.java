package org.obd.metrics.codec.batch;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractBatchCodec implements BatchCodec {
	
	private static final String[] DELIMETERS = new String[] {"1:","2:","3:","4:","5:"};
	protected static final int DEFAULT_BATCH_SIZE = 6;
	protected static final String MODE_22 = "22";
	
	private final BatchResponseMappingCache mappings = new BatchResponseMappingCache();
	
	protected final String predictedAnswerCode;
	protected final Adjustments adjustments;
	protected final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);
	protected final List<ObdCommand> commands;
	protected final String query;
	protected final Init init;
	protected final BatchCodecType codecType;

	AbstractBatchCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments,
			final String query, final List<ObdCommand> commands) {
		this.codecType = codecType;
		this.adjustments = adjustments;
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = answerCodeCodec
				.getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
		this.init = init;
	}

	@Override
	public int getCacheHit(final String query) {
		return mappings.getCacheHit(query);
	}

	@Override
	public Map<ObdCommand, ConnectorResponse> decode(final PidDefinition p, final ConnectorResponse connectorResponse) {
		final byte[] message = connectorResponse.getBytes();

		if (mappings.contains(query)) {
			return mappings.lookup(query, connectorResponse);
		} else {
			
			final int colonFirstIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), ":".getBytes(), 1, 0);
			final int codeIndexOf = Bytes.indexOf(message, connectorResponse.getLength(), predictedAnswerCode.getBytes(),
					predictedAnswerCode.length(), colonFirstIndexOf > 0 ? colonFirstIndexOf : 0);

			if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5
					|| (colonFirstIndexOf > 0 && (codeIndexOf - colonFirstIndexOf) == 1)) {

				final Map<ObdCommand, ConnectorResponse> values = new HashMap<>();
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
					values.put(command, new BatchMessage(pidMapping, connectorResponse));
					batchResponseMapping.getMappings().add(pidMapping);
					continue;

				}
				mappings.insert(query, batchResponseMapping);
				
				return values;
			} else {
				log.warn("Answer code for query: '{}' was not correct: {}", query, connectorResponse.getMessage());
			}
		}

		return Collections.emptyMap();
	}

	@Override
	public List<BatchObdCommand> encode() {
		if (commands.size() <= DEFAULT_BATCH_SIZE) {
			final Map<String, List<ObdCommand>> groupedByMode = commands.stream()
					.collect(Collectors.groupingBy(f -> f.getPid().getMode()));

			return groupedByMode.entrySet().stream().map(e -> {
				// split by partitions of $BATCH_SIZE size commands
				return ListUtils.partition(e.getValue(), determineBatchSize(e.getKey())).stream().map(partitions -> {
					return map(partitions, 0);
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		} else {

			final Map<String, Map<Integer, List<ObdCommand>>> groupedByModeAndPriority = groupByPriority();

			return groupedByModeAndPriority.entrySet().stream().map(entry -> {
				return entry.getValue().entrySet().stream().map(e -> {
					// split by partitions of $BATCH_SIZE size commands
					return ListUtils.partition(e.getValue(), determineBatchSize(entry.getKey())).stream()
							.map(partition -> {
								return map(partition, e.getKey());
							}).collect(Collectors.toList());
				}).flatMap(List::stream).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		}
	}

	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPriority() {
		return commands.stream().collect(
				Collectors.groupingBy(f -> f.getPid().getMode(), Collectors.groupingBy(p -> p.getPid().getPriority())));
	}

	protected BatchObdCommand map(final List<ObdCommand> commands, final int priority) {
		final String query = commands.get(0).getPid().getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")) + " "
				+ (adjustments.isResponseLengthEnabled() ? determineNumberOfLines(commands) : "");

		final BatchCodec codec = BatchCodec.builder()
				.codecType(codecType)
				.init(init)
				.adjustments(adjustments)
				.query(query)
				.commands(commands)
				.build();
		
		return new BatchObdCommand(codec, query, commands, priority);
	}

	protected int determineBatchSize(final String mode) {
		return DEFAULT_BATCH_SIZE;
	}

	protected int determineNumberOfLines(final List<ObdCommand> commands) {
		// 3 00B0:62194F2E65101:0348193548
		// 6 26 00E0:410BFF0C00001:11000D000400062:80AAAAAAAAAAAA
		// 5 22 00C0:410C000011001:0D0004000680AA
		// 4 1 0090:4111000D00041:000680AAAAAAAA
		// 3 1 410D0004000680
		// 2 8 4104000680
		// 10
		// 14
		final int length = getPIDsLength(commands);

		if (length < 12) {
			return 1;
		} else if (length >= 12 && length <= 24) {
			return 2;
		} else if (length >= 25 && length <= 37) {
			return 3;
		} else if (length >= 38 && length <= 49) {
			return 4;
		} else {
			return 5;
		}
	}

	
	protected int getPIDsLength(final List<ObdCommand> commands) {
		final int length = commands.stream().map(p -> p.getPid().getPid().length() + (2 * p.getPid().getLength()))
				.reduce(0, Integer::sum);
		
		final String cmd = commands.get(0).getPid().getMode() + " "
		+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" "));
		
		log.info("Calculated response length: {} for commands '{}'", length, cmd);

		return length;
	}
}
