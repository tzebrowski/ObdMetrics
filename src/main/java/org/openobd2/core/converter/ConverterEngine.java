package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConverterEngine implements Converter<Object> {

	private static final int SUCCCESS_CODE = 40;

	private final Map<String, Definition> definitions = new HashMap<>();
	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
			.collect(Collectors.toList()); // A - Z

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");

	@Builder
	public static ConverterEngine build(@NonNull @Singular("definitionFile") List<String> definitionFile) {

		final ConverterEngine instance = new ConverterEngine();
		definitionFile.forEach(f -> {
			try {
				instance.loadRules(f);
			} catch (IOException e) {
				log.error("Failed to load definitin file", e);
			}
		});
		return instance;
	}

	@Override
	public Object convert(@NonNull String rawData) {
		return convert(rawData, Object.class);
	}

	public <T> T convert(@NonNull String rawData, @NonNull Class<T> clazz) {

		final Definition definition = definitions.get(toDefinitionId(rawData));

		if (null == definition) {
			log.debug("No definition found for: {}", rawData);
		} else {
			log.debug("Found definition: {}", definition);
			if (isSuccessAnswerCode(rawData, definition)) {

				final String rawAnswerData = getRawAnswerData(rawData, definition);
				for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
					final String hexValue = rawAnswerData.substring(i, i + 2);
					jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
				}

				try {
					long time = System.currentTimeMillis();
					Object eval = jsEngine.eval(definition.getFormula());
					time = System.currentTimeMillis() - time;
					log.debug("Execution time: {}ms", time);
					return clazz.cast(eval);
				} catch (ScriptException e) {
					log.error("Failed to evaluate rule");
				}
			} else {
				log.warn("Answer code is not success for: {}", rawData);
			}
		}
		return null;
	}

	private String toDefinitionId(String rawData) {
		int pidIdLength = 4;
		if (rawData.length() > pidIdLength) {
			return rawData.substring(0, pidIdLength).toLowerCase();
		}else {
			return null;
		}
	}
	private boolean isSuccessAnswerCode(String raw, Definition rule) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode(rule));
	}

	private String getPredictedAnswerCode(Definition rule) {
		// success code = 0x40 + mode + pid
		return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(rule.getMode())) + rule.getPid()).toLowerCase();
	}

	private String getRawAnswerData(String raw, Definition rule) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode(rule).length());
	}

	private void loadRules(final String definitionFile) throws IOException, JsonParseException, JsonMappingException {
		final InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(definitionFile);
		if (null == inputStream) {
			log.error("Was not able to load file: {}", definitionFile);
		} else {
			final Definition[] readValue = objectMapper.readValue(inputStream, Definition[].class);
			log.info("Load {} rules", readValue.length);
			for (final Definition r : readValue) {
				definitions.put(getPredictedAnswerCode(r), r);
			}
		}
	}
}
