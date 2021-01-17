package org.openobd2.core.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchCommandReplyDecoder extends CommandReplyDecoder {

	public Map<String, String> decode(@NonNull final String mode, final List<PidDefinition> pids,
			@NonNull final String message) {
		final int indexOf = message.indexOf(getPredictedAnswerCode(mode));
		
		if (indexOf == 0 || indexOf == 5) {
			final String normalized = message.substring(indexOf + 2, message.length()).replace(":", "");
			final Map<String, Integer> pidLengthMap = pids.stream()
					.collect(Collectors.toMap(PidDefinition::getPid, item -> item.getLength()));

			final Map<String, String> values = new HashMap<>();
			for (int i = 0; i < normalized.length(); i++) {
				if (i + 2 < normalized.length()) {
					final String sequence = normalized.substring(i, i + 2).toUpperCase();
					if (pidLengthMap.containsKey(sequence)) {
						final String pidValue = normalized.substring(i + 2, i + 2 + (pidLengthMap.get(sequence) * 2));
						pidLengthMap.remove(sequence);
						values.put(sequence, pidValue);
					}
				}
			}
			
			return values;
		} else {
			log.warn("Answer code was not correct for message: {}", message);
			return new HashMap<>();
		}
	}
}
