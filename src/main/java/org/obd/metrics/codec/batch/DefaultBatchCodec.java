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

	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);
	private static final int MODE_01_BATCH_SIZE = 6;
	private static final int MODE_22_BATCH_SIZE = 3;

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, BatchMessageVariablePattern> cache = new HashMap<>();
	private final String query;

	DefaultBatchCodec(String query, List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = answerCodeCodec
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, RawMessage> decode(PidDefinition p, RawMessage raw) {
		final int answerCodeindexOf = indexOf(raw.getBytes(), predictedAnswerCode.getBytes(),
		        predictedAnswerCode.length(), 0);

		if (answerCodeindexOf == 0 || answerCodeindexOf == 3 || answerCodeindexOf == 5) {
			if (cache.containsKey(query)) {
				return getFromCache(raw.getBytes());
			} else {

				final Map<ObdCommand, RawMessage> values = new HashMap<>();
				final BatchMessageVariablePattern pattern = new BatchMessageVariablePattern();

				int start = answerCodeindexOf;
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
			log.warn("Answer code was not correct fo the query", query);
		}

		return Collections.emptyMap();
	}

	@Override
	public int getCacheHit(String query) {
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

	private int determineBatchSize(String mode) {
		int batchSize = MODE_01_BATCH_SIZE;
		if ("22".equals(mode)) {
			batchSize = MODE_22_BATCH_SIZE;
		}
		return batchSize;
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

	private BatchObdCommand map(List<ObdCommand> commands, int priority) {
		return new BatchObdCommand(
		        commands.get(0).getPid().getMode() + " "
		                + commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")),
		        commands, priority);
	}

	private int indexOf(byte[] value, byte[] str, int strCount, int fromIndex) {
		int valueCount = value.length;
		byte first = str[0];
		int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (value[i] != first) {
				while (++i <= max && value[i] != first)
					;
			}
			if (i <= max) {
				int j = i + 1;
				int end = j + strCount - 1;
				for (int k = 1; j < end && value[j] == str[k]; j++, k++)
					;
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}
}
