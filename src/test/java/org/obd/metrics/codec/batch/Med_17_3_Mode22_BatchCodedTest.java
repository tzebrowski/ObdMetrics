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

public class Med_17_3_Mode22_BatchCodedTest extends CodecTestRunner {	
	
	@Test
	public void case_01(){
		
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1867", 0.0);
		expectedValues.put("180E", 0.0);
		
		final String query = "22 1867 180E 2";
		final String ecuAnswer = "0090:6218670000181:0E0000";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"alfa.json"
		
		);
	}

	@Test
	public void case_02() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1867", 0.0);
		expectedValues.put("1003", 6);
		expectedValues.put("1935", 6);
		
		final String query = "22 194F 1003 1935 2";
		final String ecuAnswer = "00B0:62194F2E65101:0348193548";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				Adjustments.DEFAULT,"alfa.json"
		
		);
	}
}
