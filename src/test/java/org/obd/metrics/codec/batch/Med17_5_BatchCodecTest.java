 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;

public class Med17_5_BatchCodecTest extends BatchCodecTestRunner{
	
	@Test
	public void case_00() {
	
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("04", 0.0);
		expectedValues.put("11", 0);
		expectedValues.put("0B", 2.55);
		expectedValues.put("15", 15.12);
		expectedValues.put("0C", 0);
		
		final String query = "01 15 0B 0C 04 11";
		final String ecuAnswer = "00D0:41155AFF0BFF1:0C000004001100";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"mode01.json", "mode01_2.json"
		);
	}
	
	
	@Test
	public void case_01() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("04", 0.0);
		expectedValues.put("05", -40);
		expectedValues.put("06", 0.0);
		
		final String query = "01 04 05 06";
		final String ecuAnswer = "00F0:41010007E1001:030000040005002:0680AAAAAAAAAA";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"mode01.json", "mode01_2.json"
		);
	}

	@Test
	public void case_02() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("05", -40);
		expectedValues.put("0B", 2.55);
		expectedValues.put("0C", 0);
		expectedValues.put("0F", -40);
		expectedValues.put("11", 0);
		
		final String query = "01 05 0B 0C 0F 11";
		final String ecuAnswer = "00C0:4105000BFF0C1:00000F001100AA";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"mode01.json", "mode01_2.json"
		);
	}

	@Test
	public void case_03() {
	
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("05", -40);
		expectedValues.put("0C", 0);
		
		final String query = "01 05 0C";
		final String ecuAnswer = "4105000C0000";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"mode01.json", "mode01_2.json"
		);
	
	}

	@Test
	public void case_04() {
		
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("04", 0.0);
		expectedValues.put("05", -40);
		expectedValues.put("06", 0.0);
		expectedValues.put("07", 8.59);
		
		final String query = "01 04 05 06 07";
		final String ecuAnswer = "0110:41010007E1001:030000040005002:0680078BAAAAAA";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"mode01.json", "mode01_2.json"
		);
	}
}
