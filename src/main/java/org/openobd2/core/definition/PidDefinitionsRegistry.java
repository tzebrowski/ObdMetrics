package org.openobd2.core.definition;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PidDefinitionsRegistry {
	private static final int SUCCCESS_CODE = 40;

	private final Map<String, PidDefinition> definitions = new HashMap<>();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Builder
	public static PidDefinitionsRegistry build(@NonNull List<String> definitionFile) {

		final PidDefinitionsRegistry instance = new PidDefinitionsRegistry();
		definitionFile.forEach(f -> {
			try {
				instance.loadRules(f);
			} catch (IOException e) {
				log.error("Failed to load definitin file", e);
			}
		});
		return instance;
	}

	public PidDefinition get(String rawData) {
		return definitions.get(toDefinitionId(rawData));
	}

	private String toDefinitionId(String rawData) {
		int pidIdLength = 4;
		if (rawData.length() > pidIdLength) {
			return rawData.substring(0, pidIdLength).toLowerCase();
		} else {
			return null;
		}
	}

	private void loadRules(final String definitionFile) throws IOException, JsonParseException, JsonMappingException {
		final InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(definitionFile);
		if (null == inputStream) {
			log.error("Was not able to load file: {}", definitionFile);
		} else {
			final PidDefinition[] readValue = objectMapper.readValue(inputStream, PidDefinition[].class);
			log.info("Load {} rules", readValue.length);
			for (final PidDefinition r : readValue) {
				definitions.put(getPredictedAnswerCode(r), r);
			}
		}
	}

	private String getPredictedAnswerCode(PidDefinition rule) {
		// success code = 0x40 + mode + pid
		return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(rule.getMode())) + rule.getPid()).toLowerCase();
	}
}
