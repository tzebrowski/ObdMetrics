package org.obd.metrics.command.obd;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.AnswerCodeDecoder;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCodec implements BatchCodec {

	private static final String NORMALIZATION_PATTERN = "[a-zA-Z0-9]{1}\\:";
	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, Pattern> cache = new HashMap<>();
	private final String query;

	DefaultBatchCodec(String query, List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.predictedAnswerCode = new AnswerCodeDecoder()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, String> decode(PidDefinition p, String message) {

		final String normalized = message.replaceAll(NORMALIZATION_PATTERN, "");
		int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);
		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			if (cache.containsKey(query)) {
				return getFromCache(normalized);
			} else {
				final Map<ObdCommand, String> values = new HashMap<ObdCommand, String>();
				int messageIndex = indexOfAnswerCode + 2;
				final Pattern pattern = new Pattern();
				for (final ObdCommand command : commands) {

					if (messageIndex == normalized.length()) {
						break;
					}

					final PidDefinition pid = command.pid;
					final int sizeOfPid = messageIndex + 2;
					final String pidSeq = normalized.substring(messageIndex, sizeOfPid);
					if (pidSeq.equalsIgnoreCase(pid.getPid())) {

						final int pidLength = pid.getLength() * 2;
						final String pidValue = normalized.substring(sizeOfPid, sizeOfPid + pidLength);

						if (log.isTraceEnabled()) {
							log.trace("Init: {} =  {} : {} : {}", pidSeq, sizeOfPid, (sizeOfPid + pidLength), pidValue);
						}

						values.put(command, predictedAnswerCode + pid.getPid() + pidValue);
						pattern.entries.add(new PatternEntry(command, sizeOfPid, (sizeOfPid + pidLength)));
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

	int getHit(String q) {
		return cache.get(q).hit;
	}

	private Map<ObdCommand, String> getFromCache(final String message) {
		final Map<ObdCommand, String> values = new HashMap<ObdCommand, String>();
		final Pattern pattern = cache.get(query);
		pattern.hit++;
		pattern.entries.forEach(p -> {
			final String value = message.substring(p.start, p.end);
			if (log.isTraceEnabled()) {
				log.info("Cache: {} = {} : {} : {}", p.command.pid.getPid(), p.start, p.end, value);
			}
			values.put(p.command, predictedAnswerCode + p.command.pid.getPid() + value);
		});
		return values;
	}
}
