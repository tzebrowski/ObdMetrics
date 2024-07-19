/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.codec.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;

public class StrictPolicy_CodecTest extends CodecTestRunner {
	
	private final Adjustments adjustments = Adjustments.builder()
			.batchPolicy(BatchPolicy
					.builder()
					.enabled(true)
					.strictValidationEnabled(true)
					.build())
			.build();
	
	@Test
	public void case02() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 800);
		expectedValues.put("04", 22.35);
		expectedValues.put("0D", 65);
		expectedValues.put("05", 91);
		expectedValues.put("42", 13.247);
		
		
		final String query = "01 0C 04 0D 05 42 2";
		final String ecuAnswer = "00D0:410C0C80043900D0:410C0C7C0439";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer, ValidationStrategy.INVALID_DATA)),
				adjustments,"mode01.json"
		
		);
	}	
	
	
	@Test
	public void case03() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 800);
		expectedValues.put("04", 21.96);
		expectedValues.put("0D", 0);
		expectedValues.put("05", 91);
		expectedValues.put("42", 13.247);
	
		final String query = "01 0C 04 0D 05 42 2";
		final String a1 = "00D0:410C0C80043900D0:410C0C7C0439";
		final String a2 = "00D0:410C0C8004381:0D0005834233BF";
		
		runTest(query, Arrays.asList(
				new ValidationInput(expectedValues, a2),
				new ValidationInput(expectedValues, a1, ValidationStrategy.INVALID_DATA)),
				adjustments,"mode01.json"
		);

	}

	@Test
	public void case_00() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("15", 0);
		expectedValues.put("0B", 0.0);
		expectedValues.put("0C", 1.02);
		expectedValues.put("04", 0);
		expectedValues.put("11", 0);
		expectedValues.put("0E", 0);
		expectedValues.put("0F", 0);
		expectedValues.put("05", 0);
		
		final String query = "01 15 0B 0C 04 11 0E 0F 05 2";
		final String ecuAnswer = "01150B0C0411200D0:41155AFF0BFF1:0C000004001100";

		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer, ValidationStrategy.INVALID_DATA)),
				adjustments,"mode01.json");
	}
}
