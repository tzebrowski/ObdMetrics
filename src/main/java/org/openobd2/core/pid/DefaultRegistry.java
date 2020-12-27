package org.openobd2.core.pid;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

	@Override
	public PidDefinition findByAnswerRawData(String rawData) {
		return definitions.get(toDefinitionId(rawData));
	}

	@Override
	public PidDefinition findBy(@NonNull String mode, @NonNull String pid) {
		return definitions.get((mode + pid).toLowerCase());
	}

	private String toDefinitionId(String rawData) {
		int pidIdLength = 4;
		if (rawData.length() > pidIdLength) {
			return rawData.substring(0, pidIdLength).toLowerCase();
		} else {
			return null;
		}
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load pids configuration");
			} else {
				final PidDefinition[] readValue = objectMapper.readValue(inputStream, PidDefinition[].class);
				log.info("Load {} pid definitions", readValue.length);
				for (final PidDefinition pidDef : readValue) {
					definitions.put(
							(String.valueOf(40 + Integer.valueOf(pidDef.getMode())) + pidDef.getPid()).toLowerCase(),
							pidDef);
					definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
				}
			}
		} catch (IOException e) {
			log.error("Failed to load definitin file", e);
		}
	}
}
