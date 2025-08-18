/**
* Copyright 2019-2025, Tomasz Żebrowski
*
* <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
* agreements. See the NOTICE file distributed with this work for additional information regarding
* copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance with the License. You may obtain a
* copy of the License at
*
* <p>http://www.apache.org/licenses/LICENSE-2.0
*
* <p>Unless required by applicable law or agreed to in writing, software distributed under the
* License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.decoder.BatchMessageBuilder.instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Giulietta_175TBI_CodecTest extends BatchCodecTestRunner {

	@ParameterizedTest
	@CsvSource(value = { 
			"196D0000=00", 
			"196D0A11=20.1", 
			"196D09D0=19.6", 
			"196D0FFF=32.0", 
			"196DFFF2=-0.1",
			"196D0A24=20.3", 
			"196DFF11=-1.9", 
			"196DFF99=-0.80", 
			"196DFF99=-0.80", 
			"196DEE99=-34.8", 
			"196DDD99=-68.8",
			"196DCCFF=-102", 
			"196DBBFF=-136", 
			"196DAAFF=-170", 
			"196D99FF=-204", 
			"196D88FF=-238",
			"196D77FF=240", }, delimiter = '=')
	public void camshaftDesiredTest(String camshaftDesiredGiven, Double camshaftDesiredDecoded) {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("196D", camshaftDesiredDecoded);
		expectedValues.put("196A", 6.25);
		expectedValues.put("197B", 0.0);
		expectedValues.put("196C", 20.0);
		expectedValues.put("1970", 6.25);
		expectedValues.put("197C", 0.0);
		expectedValues.put("197D", 0.0);

		final String query = "196D 196A 197B 196C 1970 197C 197D";
		final String ecuAnswer = String.format("01D0:62%s0A24191:6A1000197B00002:196C0A001970103:00197C0000197D4:0000",
				camshaftDesiredGiven);

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)), ADJUSTEMENTS, "alfa.json");
	}

	@Test
	public void camshaftDesiredCommaTest() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("196D", -12.4);
		final String query = "196E 197B 197D 197A 196D 196C 197C";
		final String ecuAnswer = String
				.format("01D0:62196E18B2191:7BEFD1197DF0642:197A0883196DF93:D1196CFA64197C4:0978");

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)), ADJUSTEMENTS, "alfa.json");
	}

	@Disabled
	@Test
	public void camshaftDesired3Test() {
		final String query = "196E 197B 197D 197A 196D 196C 197C";
		
		final CodecRegistry codecRegistry = CodecRegistry.builder().adjustments(BatchCodecTestRunner.ADJUSTEMENTS)
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().debug(true).build()).build();

		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("196D", 21.6);
		
		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());

        final Path filePath = Paths.get("src/test/resources/signed_value_comma_input.txt");
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach( p->{
            	if (p.contains("RX:")) {
            		int delimeter = p.indexOf(",");
            		if (delimeter > 0) {
            			final  String ecuAnswer = p.substring(4, delimeter);
						final BatchCodec codec = BatchCodec.builder().query(query).adjustments(BatchCodecTestRunner.ADJUSTEMENTS).commands(commands).build();
						List<ValidationInput> input = Arrays.asList(new ValidationInput(expectedValues, ecuAnswer));
						
						for (final ValidationInput validationInput : input) {

							final byte[] messageBytes = validationInput.getMessage().getBytes();
							final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(messageBytes));

							final ConnectorResponse connectorResponse = instance(messageBytes);

							if (validationInput.getStrategy() == ValidationStrategy.DEFAULT) {
								Assertions.assertThat(values).isNotEmpty();
								Assertions.assertThat(values).hasSize(commands.size());

								for (final ObdCommand cmd : commands) {
									Assertions.assertThat(values).containsEntry(cmd, connectorResponse);
								}

								commands.forEach(c -> {
									final ConnectorResponse cr = values.get(c);
									final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
									final String pid = c.getPid().getPid();
									Object expected = validationInput.getExpectedValues().get(pid);
									
									if (expected == null) {
										expected = validationInput.getExpectedValues().get(c.getPid().getId());
									}
									System.err.println(value);
								});
							} 
						}
            		
            		
            		}
            	}
            });
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
	}
}
