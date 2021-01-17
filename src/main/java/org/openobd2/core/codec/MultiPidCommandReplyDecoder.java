package org.openobd2.core.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openobd2.core.pid.PidDefinition;

public class MultiPidCommandReplyDecoder {
	int code = 40;

	public Map<String, String> decode(final String mode,final List<PidDefinition> pids, final String message) {
		final Map<String, String> values = new HashMap<>();
		int indexOf =  message.indexOf(String.valueOf(code + Integer.parseInt(mode)));
		
		if (indexOf >= 0) {
			final String normalized = message.substring(indexOf + 2,message.length()).replace(":","");
			final Map<String, Integer> pidLengthMap = pids.stream()
					.collect(Collectors.toMap(PidDefinition::getPid, item -> item.getLength()));

			for (int i = 0, n = normalized.length(); i < n; i++) {
				if (i + 2 < normalized.length()) {
					final String sequence = normalized.substring(i, i + 2).toUpperCase();
					if (pidLengthMap.containsKey(sequence)) {
						final String pidValue = normalized.substring(i + 2, i + 2 + (pidLengthMap.get(sequence) * 2));
						pidLengthMap.remove(sequence);
						values.put(sequence, pidValue);
					}
				}
			}
		}
		return values;
	}
}
