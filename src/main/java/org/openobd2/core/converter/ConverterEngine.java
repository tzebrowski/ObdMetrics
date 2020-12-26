package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.collections4.map.HashedMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConverterEngine {

	private final Map<String, Definition> definitions = new HashedMap<String, Definition>();
	private final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
			.collect(Collectors.toList());

	@Builder
	public static ConverterEngine build(@NonNull String definitionFile)
			throws JsonParseException, JsonMappingException, IOException {

		final ConverterEngine engine = new ConverterEngine();
		engine.loadRules(definitionFile);
		return engine;
	}

	void loadRules(final String definitionFile) throws IOException, JsonParseException, JsonMappingException {
		final ObjectMapper objectMapper = new ObjectMapper();
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

	public Object convert(String rawData) throws ScriptException {

		final Definition rule = definitions.get(rawData.substring(0, 4));

		if (null == rule) {
			log.error("No rule found for: {}", rawData);
		} else {

			if (isSuccessAnswerCode(rawData, rule)) {

				final String rawAnswerData = getRawAnswerData(rawData, rule);
				for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
					final String hexValue = rawAnswerData.substring(i, i + 2);
					final String paramName = params.get(j);
					final int decimalValue = Integer.parseInt(hexValue, 16);
					jsEngine.put(String.valueOf(paramName), decimalValue);
				}

				long time = System.currentTimeMillis();
				final Object eval = jsEngine.eval(rule.getFormula());
				time = System.currentTimeMillis() - time;
				log.debug("Execution time: {}ms", time);
				return eval;
			} else {
				log.warn("Answer code is not success for: {}", rawData);
			}
		}
		return null;
	}

	boolean isSuccessAnswerCode(String raw, Definition rule) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode(rule));
	}

	String getPredictedAnswerCode(Definition rule) {
		// success code = 0x40 + mode + pid
		return String.valueOf(40 + Integer.valueOf(rule.getMode())) + rule.getPid();
	}

	String getRawAnswerData(String raw, Definition rule) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode(rule).length());
	}

}
