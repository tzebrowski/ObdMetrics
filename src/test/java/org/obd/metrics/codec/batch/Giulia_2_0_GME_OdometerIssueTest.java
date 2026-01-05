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

public class Giulia_2_0_GME_OdometerIssueTest extends BatchCodecTestRunner {
	
	@Test
	public void case001() {
		//1942 2805 1937 130A 2001 18F0 1935 1302 18BA 1004
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1942", 0.0);
		expectedValues.put("2805", 0);
		expectedValues.put("1937", 998);
		expectedValues.put("130A", 1.0);
		expectedValues.put("2001", 65899.9);
		expectedValues.put("18F0", 0);
		expectedValues.put("1935", 18);
		expectedValues.put("1302", 19);
		expectedValues.put("18BA", 490);
		expectedValues.put("1004", 12.0);
		
		// 0A0E37
		final String query = "1942 2805 1937 130A 2001 18F0 1935 1302 18BA 1004";
		final String ecuAnswer = "0250:6219420028051:0000193703E6132:0A1920010A0E373:18F00019353A134:02001318BA6C105:040078";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}
	
	@Test
	public void case02() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1942", 0.0);
		expectedValues.put("2805", 0);
		expectedValues.put("1937", 998);
		expectedValues.put("2001", 324701.4);
		expectedValues.put("18F0", 0);
		expectedValues.put("1935", 18);
		expectedValues.put("1302", 19);
		expectedValues.put("18BA", 490);
		expectedValues.put("1004", 12.0);
		expectedValues.put("1003", 19);
		//318BA6
		final String query = "1942 2805 1937 2001 18F0 1935 1302 18BA 1004 1003";
		final String ecuAnswer = "0250:6219420028051:0000193703E6202:010A0E3718F0003:19353A130200134:18BA6C100400785:10033B";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}
	
	
	@Test
	public void case03() {

		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1942", 0.0);
		expectedValues.put("2805", 0);
		expectedValues.put("1937", 998);
		expectedValues.put("2001", 66022.2);
		expectedValues.put("18F0", 0);
		expectedValues.put("1935", 58);
		expectedValues.put("1302", 63);
		expectedValues.put("18BA", 520);
		expectedValues.put("1004", 12.3);
		expectedValues.put("1003", 62);
		//318BA6
		final String query = "1942 2805 1937 18F0 1935 1302 18BA 1004 1003 2001";
		final String ecuAnswer = "0250:6219420028051:0000193703E6182:F00019356213023:003F18BA7210044:007B10036620015:0A12FE";

		runTest(query, Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)));
	}

}
