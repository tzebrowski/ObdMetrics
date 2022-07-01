package org.obd.metrics.codec.batch;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCodec implements BatchCodec {

	private final boolean FEATURE_RESPONSE_LENGTH_ENABLED = true;

	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);
	private static final int MODE_01_BATCH_SIZE = 6;
	private static final int MODE_22_BATCH_SIZE = 3;
	private static final String MODE_22 = "22";

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, BatchMessageVariablePattern> cache = new HashMap<>();
	private final String query;

	DefaultBatchCodec(final String query, final List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = answerCodeCodec
				.getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, RawMessage> decode(final PidDefinition p, final RawMessage raw) {
		final int codeIndexOf = indexOf(raw.getBytes(), predictedAnswerCode.getBytes(), predictedAnswerCode.length(),
				0);

		if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5) {
			if (cache.containsKey(query)) {
				return getFromCache(raw.getBytes());
			} else {

				final Map<ObdCommand, RawMessage> values = new HashMap<>();
				final BatchMessageVariablePattern pattern = new BatchMessageVariablePattern();

				int start = codeIndexOf;
				final byte[] bytes = raw.getBytes();

				for (final ObdCommand command : commands) {

					final PidDefinition pidDefinition = command.getPid();

					String pidId = pidDefinition.getPid();
					int pidLength = pidId.length();
					int pidIdIndexOf = indexOf(bytes, pidId.getBytes(), pidLength, start);

					log.info("Found pid={}, indexOf={}", pidId, pidIdIndexOf);

					if (pidIdIndexOf == -1) {
						if (pidLength == 4) {
							pidId = pidId.substring(0, 2) + "1:" + pidId.substring(2, 4);
							pidLength = pidId.length();
							pidIdIndexOf = indexOf(bytes, pidId.getBytes(), pidLength, start);
							log.info("Another iteration. Found pid={}, indexOf={}", pidId, pidIdIndexOf);
						}

						if (pidIdIndexOf == -1) {
							continue;
						}
					}

					start = pidIdIndexOf + pidLength;

					if ((char) bytes[start] == ':' || (char) bytes[start + 1] == ':') {
						start += 2;
					}

					final int end = start + (pidDefinition.getLength() * 2);
					final BatchMessageVariablePatternItem messagePattern = new BatchMessageVariablePatternItem(command,
							start, end);
					values.put(command, new BatchMessage(messagePattern, bytes));
					pattern.getItems().add(messagePattern);
					continue;

				}
				cache.put(query, pattern);
				return values;
			}
		} else {
			log.warn("Answer code was not correct: {}", raw.getMessage());
		}

		return Collections.emptyMap();
	}

	@Override
	public int getCacheHit(final String query) {
		return cache.get(query).getHit();
	}

	@Override
	public List<BatchObdCommand> encode() {
		if (commands.size() <= MODE_01_BATCH_SIZE) {
			final Map<String, List<ObdCommand>> groupedByMode = commands.stream()
					.collect(Collectors.groupingBy(f -> f.getPid().getMode()));

			return groupedByMode.entrySet().stream().map(e -> {
				// split by partitions of $BATCH_SIZE size commands
				return ListUtils.partition(e.getValue(), determineBatchSize(e.getKey())).stream().map(partitions -> {
					return map(partitions, 0);
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		} else {

			final Map<String, Map<Integer, List<ObdCommand>>> groupedByModeAndPriority = commands.stream()
					.collect(Collectors.groupingBy(f -> f.getPid().getMode(),
							Collectors.groupingBy(p -> p.getPid().getPriority())));

			return groupedByModeAndPriority.entrySet().stream().map(entry -> {
				return entry.getValue().entrySet().stream().map(e -> {
					// split by partitions of $BATCH_SIZE size commands
					return ListUtils.partition(e.getValue(), determineBatchSize(entry.getKey())).stream()
							.map(partitions -> {
								return map(partitions, e.getKey());
							}).collect(Collectors.toList());
				}).flatMap(List::stream).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		}
	}

	private Map<ObdCommand, RawMessage> getFromCache(final byte[] message) {
		final Map<ObdCommand, RawMessage> values = new HashMap<>();
		final BatchMessageVariablePattern pattern = cache.get(query);

		pattern.updateCacheHit();

		pattern.getItems().forEach(it -> {
			values.put(it.getCommand(), new BatchMessage(it, message));
		});

		return values;
	}

	private BatchObdCommand map(final List<ObdCommand> commands, final int priority) {

		return new BatchObdCommand(
				commands.get(0).getPid().getMode() + " "
						+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")) + " "
						+ (FEATURE_RESPONSE_LENGTH_ENABLED ? determineNumberOfLines(commands) : ""),
				commands, priority);
	}

	private int determineBatchSize(final String mode) {
		return MODE_22.equals(mode) ? MODE_22_BATCH_SIZE : MODE_01_BATCH_SIZE;
	}

	private int determineNumberOfLines(final List<ObdCommand> commands) {
		// 3 00B0:62194F2E65101:0348193548
		// 6 26 00E0:410BFF0C00001:11000D000400062:80AAAAAAAAAAAA
		// 5 22 00C0:410C000011001:0D0004000680AA
		// 4 1 0090:4111000D00041:000680AAAAAAAA
		// 3 1 410D0004000680
		// 2 8 4104000680
		// 10
		// 14
		// 14

		final int length = commands.stream().map(p -> p.getPid().getPid().length() + (2 * p.getPid().getLength()))
				.reduce(0, Integer::sum);

		if (length < 12) {
			return 1;
		} else if (length >= 12 && length <= 24) {
			return 2;
		} else {
			return 3;
		}
	}

	private int indexOf(final byte[] value, final byte[] str, final int strCount, final int fromIndex) {
		final int valueCount = value.length;
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
}
