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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Med17_3_BatchCodecTest extends CodecTestRunner {

	@Test
	public void case_01() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		expectedValues.put(99L, 1.02);
		expectedValues.put("0D", 0);
		
		final String query = "01 0C 10 0B 0D 2";
		final String ecuAnswer = "00B0:410C000010001:000B660D000000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
		
	}

	@Test
	public void case_02() {
	
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		expectedValues.put(99L, 1.02);
		expectedValues.put("0D", 0);
		expectedValues.put("05", -6);
		expectedValues.put("0F", 15);
			
		final String query = "01 0C 10 0B 0D 05 0F 2";
		final String ecuAnswer = "00F0:410C000010001:000B660D0005222:0F370000000000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

	@Test
	public void case_03() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		
			
		final String query = "01 0C 10 1";
		final String ecuAnswer = "410C0000100000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

	@Test
	public void case_04() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		expectedValues.put(99L, 1.02);
		
			
		final String query = "01 0C 10 0B 2";
		final String ecuAnswer = "0090:410C000010001:000B6600000000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

	@Test
	public void case_05() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		expectedValues.put(99L, 1.02);
		expectedValues.put("0D", 0);
		expectedValues.put("05", -6);
			
		final String query = "01 0C 10 0B 0D 05 2";
		final String ecuAnswer = "00D0:410C000010001:000B660D000522";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

	@Test
	public void case_06() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("10", 0.0);
		expectedValues.put(99L, 1.02);
		expectedValues.put("0D", 0);
		expectedValues.put("05", -6);
		expectedValues.put("11", 15);
			
		final String query = "01 0C 10 0B 0D 05 11 3";
		final String ecuAnswer = "00F0:410C000010001:000B660D0005222:11260000000000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

	@Test
	public void case_07() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put(99L, 1.01);
		expectedValues.put("0D", 0);
		expectedValues.put("0E", 0.0);
		expectedValues.put("0F", 7);
		expectedValues.put("10", 0.0);
			
		final String query = "01 0B 0C 0D 0E 0F 10 3";
		final String ecuAnswer = "00F0:410B650C00001:0D000E800F2F102:00000000000000";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}
	
	@Test
	public void case_08() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 800);
		expectedValues.put("04", 21.96);
		expectedValues.put("0D", 0);
		expectedValues.put("05", 91);
		expectedValues.put("42", 13.247);
	

		final String query = "01 0C 04 0D 05 42 2";
		final String ecuAnswer = "00D0:410C0C8004381:0D0005834233BF";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}
}
