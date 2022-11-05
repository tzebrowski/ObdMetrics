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
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractBatchCodec implements BatchCodec {
	
	private static final String[] DELIMETERS = new String[] {"1:","2:","3:","4:","5:"};
	protected static final int DEFAULT_BATCH_SIZE = 6;
	protected static final String MODE_22 = "22";
	
	protected final Map<String, BatchMessageVariablePattern> cache = new HashMap<>();
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
		return cache.get(query).getHit();
	}

	@Override
	public Map<ObdCommand, RawMessage> decode(final PidDefinition p, final RawMessage raw) {
		final byte[] message = raw.getBytes();
		
		final int colonFirstIndexOf = indexOf(message, raw.getLength(), ":".getBytes(), 1, 0);

		final int codeIndexOf = indexOf(message,raw.getLength(), predictedAnswerCode.getBytes(), 
				predictedAnswerCode.length(), colonFirstIndexOf > 0 ? colonFirstIndexOf : 0);

		if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5
				|| (colonFirstIndexOf > 0 && (codeIndexOf - colonFirstIndexOf) == 1)) {
			if (cache.containsKey(query)) {
				return getFromCache(message);
			} else {

				final Map<ObdCommand, RawMessage> values = new HashMap<>();
				final BatchMessageVariablePattern pattern = new BatchMessageVariablePattern();

				int start = codeIndexOf;
			
				final byte[] messageCpy = raw.copy();
				
				for (final ObdCommand command : commands) {

					final PidDefinition pidDefinition = command.getPid();

					String pidId = pidDefinition.getPid();
					int pidLength = pidId.length();
					int pidIdIndexOf = indexOf(message, raw.getLength(), pidId.getBytes(), pidLength, start);

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
								pidIdIndexOf = indexOf(message, raw.getLength(), pidId.getBytes(), pidLength, start);

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
					final BatchMessageVariablePatternItem messagePattern = new BatchMessageVariablePatternItem(command,
							start, end);
					values.put(command, new BatchMessage(messagePattern, messageCpy));
					pattern.getItems().add(messagePattern);
					continue;

				}
				cache.put(query, pattern);
				return values;
			}
		} else {
			log.warn("Answer code for query: '{}' was not correct: {}", query, raw.getMessage());
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

	protected int indexOf(final byte[] value, int valueLength, final byte[] str, final int strCount,
			final int fromIndex) {
		final int valueCount = valueLength;
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (value[i] != first) {
				while (++i <= max && value[i] != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && value[j] == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}

	protected Map<ObdCommand, RawMessage> getFromCache(final byte[] message) {
		final Map<ObdCommand, RawMessage> values = new HashMap<>();
		final BatchMessageVariablePattern pattern = cache.get(query);

		pattern.updateCacheHit();

		pattern.getItems().forEach(it -> {
			values.put(it.getCommand(), new BatchMessage(it, message));
		});

		return values;
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
