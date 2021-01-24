package org.obd.metrics.pid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.obd.metrics.codec.MetricsDecoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements PidRegistry {

	private final Map<String, PidDefinition> definitions = new HashMap<>();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private MetricsDecoder decoder = new MetricsDecoder();
	private String mode;

	@Override
	public PidDefinition findByAnswerRawData(String rawData) {
		final String answerCode = decoder.getAnswerCode(rawData);
		log.debug("Answer code: {}", answerCode);
		return definitions.get(answerCode);
	}

	@Override
	public PidDefinition findBy(String pid) {
		return findBy(this.mode, pid);
	}

	@Override
	public PidDefinition findBy(@NonNull String mode, @NonNull String pid) {
		return definitions.get((mode + pid).toLowerCase());
	}

	public Collection<PidDefinition> getDefinitions() {
		return new HashSet<PidDefinition>(definitions.values());
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load pids configuration");
			} else {
				final PidDefinition[] readValue = objectMapper.readValue(inputStream, PidDefinition[].class);
				log.info("Load {} pid definitions", readValue.length);
				for (final PidDefinition pidDef : readValue) {
					definitions.put(decoder.getPredictedAnswerCode(pidDef), pidDef);
					definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
				}

				//
				this.mode = readValue[0].getMode();
			}
		} catch (IOException e) {
			log.error("Failed to load definitin file", e);
		}
	}

}
