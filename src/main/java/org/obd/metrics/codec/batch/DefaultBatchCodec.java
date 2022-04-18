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
	private static final int BATCH_SIZE = 6;
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

					final PidDefinition pid = command.getPid();

					final int pidLength = pid.getPid().length();
					final int pidIndexOf = indexOf(bytes, pid.getPid().getBytes(), pidLength, start);

					log.info("Pid={}, indexOf={}", pid.getPid(), pidIndexOf);

					if (pidIndexOf == -1) {
						continue;
					}

					start = pidIndexOf + pidLength;

					if ((char) bytes[start] == ':' || (char) bytes[start + 1] == ':') {
						start += 2;
					}

					final int end = start + (pid.getLength() * 2);
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
		if (commands.size() <= BATCH_SIZE) {
			// no splitting into groups, fetch all pids at once
			return ListUtils.partition(commands, BATCH_SIZE).stream().map(partitions -> {
				return map(partitions, 0);
			}).collect(Collectors.toList());
		} else {

			final Map<Integer, List<ObdCommand>> groupedByPriority = commands.stream()
			        .collect(Collectors.groupingBy(p -> p.getPid().getPriority()));

			return groupedByPriority.entrySet().stream().map(entry -> {
				// split by partitions of $BATCH_SIZE size commands
				return ListUtils.partition(entry.getValue(), BATCH_SIZE).stream().map(partitions -> {
					return map(partitions, entry.getKey());
				}).collect(Collectors.toList());
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
