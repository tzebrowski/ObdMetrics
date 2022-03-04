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
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCodec implements BatchCodec {

	private static final int BATCH_SIZE = 6;
	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, BatchMessagePattern> cache = new HashMap<>();
	private final String query;

	DefaultBatchCodec(String query, List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = new AnswerCodeCodec()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, RawMessage> decode(PidDefinition p, RawMessage raw) {
		final String message = raw.getMessage();
		int indexOfAnswerCode = message.indexOf(predictedAnswerCode);
		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			if (cache.containsKey(query)) {
				return getFromCache(message);
			} else {
				final Map<ObdCommand, RawMessage> values = new HashMap<>();
				int messageIndex = indexOfAnswerCode + 2;
				final BatchMessagePattern pattern = new BatchMessagePattern();

				for (final ObdCommand command : commands) {
					if (messageIndex == message.length()) {
						break;
					}

					final PidDefinition pid = command.getPid();
					final int start = messageIndex + 2;
					final String pidSeq = message.substring(messageIndex, start);
					if (pidSeq.equalsIgnoreCase(pid.getPid())) {

						final int pidLength = pid.getLength() * 2;
						final int end = start + pidLength;
						if (log.isTraceEnabled()) {
							log.trace("Init: {} =  {} : {} ", pidSeq, start, end);
						}

						final BatchMessagePatternEntry messagePattern = new BatchMessagePatternEntry(command, start,
						        end);
						values.put(command, new BatchMessage(message, messagePattern));
						pattern.getEntries()
						        .add(messagePattern);
						messageIndex += pidLength + 2;
						continue;
					}
				}
				cache.put(query, pattern);
				return values;
			}
		} else {
			log.warn("Answer code was not correct for message: {}. Query: {}", message, query);
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

	private Map<ObdCommand, RawMessage> getFromCache(final String message) {
		final Map<ObdCommand, RawMessage> values = new HashMap<>();
		final BatchMessagePattern pattern = cache.get(query);

		pattern.updateCacheHit();

		pattern.getEntries().forEach(it -> {
			values.put(it.getCommand(), new BatchMessage(message, it));
		});

		return values;
	}

	private BatchObdCommand map(List<ObdCommand> commands, int priority) {
		return new BatchObdCommand(
		        commands.get(0).getPid().getMode() + " "
		                + commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")),
		        commands, priority);
	}
}
