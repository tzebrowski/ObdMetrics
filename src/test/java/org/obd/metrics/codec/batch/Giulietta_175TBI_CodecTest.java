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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
			"196D77FF=240",
			}, delimiter = '=')
	public void camshaftDesiredTest(String camshaftDesiredGiven,Double camshaftDesiredDecoded) {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("196D", camshaftDesiredDecoded);
		expectedValues.put("196A", 6.25);
		expectedValues.put("197B", 0.0);
		expectedValues.put("196C", 20.0);
		expectedValues.put("1970", 6.25);
		expectedValues.put("197C", 0.0);
		expectedValues.put("197D", 0.0);
		
		final String query = "196D 196A 197B 196C 1970 197C 197D";
		final String ecuAnswer = String.format("01D0:62%s0A24191:6A1000197B00002:196C0A001970103:00197C0000197D4:0000", camshaftDesiredGiven);

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)), ADJUSTEMENTS, "alfa.json");
	}

}
