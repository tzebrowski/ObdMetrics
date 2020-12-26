package org.openobd2.core;

import java.io.IOException;

import javax.script.ScriptException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.ConverterEngine;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ConveterEngineTest {
	@Test
	public void engineTempTest() throws JsonParseException, JsonMappingException, IOException, ScriptException {
		
		final String definitionFile = "rules.json";

		ConverterEngine converterEngine = ConverterEngine.builder().definitionFile(definitionFile).build();
		converterEngine.convert("410522");
		
		
		String rawData = "410522";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6);

		rawData = "410517";
		temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17);
	}
}
