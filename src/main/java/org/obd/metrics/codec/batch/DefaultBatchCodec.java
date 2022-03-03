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
import org.obd.metrics.raw.Raw;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCodec implements BatchCodec {
	
	private static final int BATCH_SIZE = 6;
	private static final String NORMALIZATION_PATTERN = "[a-zA-Z0-9]{1}\\:";
	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, BatchCommandPattern> cache = new HashMap<>();
	private final String query;

	DefaultBatchCodec(String query, List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = new AnswerCodeCodec()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, Raw> decode(PidDefinition p, Raw raw) {
		final String message = raw.getMessage();
		final String normalized = message.replaceAll(NORMALIZATION_PATTERN, "");
		int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);
		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			if (cache.containsKey(query)) {
				return getFromCache(normalized);
			} else {
				final Map<ObdCommand, Raw> values = new HashMap<ObdCommand, Raw>();
				int messageIndex = indexOfAnswerCode + 2;
				final BatchCommandPattern pattern = new BatchCommandPattern();
				for (final ObdCommand command : commands) {

					if (messageIndex == normalized.length()) {
						break;
					}

					final PidDefinition pid = command.getPid();
					final int sizeOfPid = messageIndex + 2;
					final String pidSeq = normalized.substring(messageIndex, sizeOfPid);
					if (pidSeq.equalsIgnoreCase(pid.getPid())) {

						final int pidLength = pid.getLength() * 2;
						final String pidValue = normalized.substring(sizeOfPid, sizeOfPid + pidLength);

						if (log.isTraceEnabled()) {
							log.trace("Init: {} =  {} : {} : {}", pidSeq, sizeOfPid, (sizeOfPid + pidLength), pidValue);
						}

						values.put(command,Raw.instance(predictedAnswerCode + pid.getPid() + pidValue));
						pattern.getEntries()
						        .add(new BatchCommandPatternEntry(command, sizeOfPid, (sizeOfPid + pidLength)));
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

	private Map<ObdCommand, Raw> getFromCache(final String message) {
		final Map<ObdCommand, Raw> values = new HashMap<ObdCommand, Raw>();
		final BatchCommandPattern pattern = cache.get(query);
		pattern.updateCacheHit();
		pattern.getEntries().forEach(p -> {
			final String value = message.substring(p.getStart(), p.getEnd());
			if (log.isTraceEnabled()) {
				log.info("Cache: {} = {} : {} : {}", p.getCommand().getPid().getPid(), p.getStart(), p.getEnd(), value);
			}
			values.put(p.getCommand(), Raw.instance(predictedAnswerCode + p.getCommand().getPid().getPid() + value));
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
