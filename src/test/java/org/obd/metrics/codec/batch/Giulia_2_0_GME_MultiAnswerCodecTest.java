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

public class Giulia_2_0_GME_MultiAnswerCodecTest extends Giulia_2_0_GME_CodecTest {

	@Test
	public void case_01() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "0C 11 0E";
		final String a1 = "0080:410C0000112D410C00001:0E800000000000";
		final String a2 = "0080:410C0000112D1:0E800000000000410C0000";

		runTest(expectedValues, query, Arrays.asList(a1, a2));
	}

	@Test
	public void case_02() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("15", 17.44);
		expectedValues.put("04", 0.0);
		expectedValues.put("06", 0.0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "15 04 06 11 0E";
		final String a1 = "00C0:4115078004001:0680112D0E8000410400";
		final String a2 = "00C0:4115078004004104001:0680112D0E8000";

		runTest(expectedValues, query, Arrays.asList(a1, a2, a1, a2, a1, a2));
	}

	@Test
	public void case_03() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("04", 0.0);
		expectedValues.put("06", 0.0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);
		expectedValues.put("05", null);

		final String query = "0C 04 06 11 0E 05";
		final String a4 = "00E0:410C000004001:0680112D0E80052:350000000000000080:410C000004001:0535AAAAAAAAAA";
		final String a1 = "00E0:410C000004001:0680112D0E80050080:410C000004002:350000000000001:0535AAAAAAAAAA";
		final String a2 = "00E0:410C000004001:0680112D0E80050080:410C000004001:0535AAAAAAAAAA2:35000000000000";

		runTest(expectedValues, query, Arrays.asList(a4, a1, a2));
	}
}
