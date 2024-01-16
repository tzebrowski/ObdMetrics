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
package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// "formula": "print(A*256); x=A; if (A * 256 > 5000) { x = (A * 256) / 57.6 |0 } x; ",
public class CatalystTempTest implements Giulia_2_0_GME_Test {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218370B=5",
			"6218370C=10",
			"6218370E=20",
			"6218370F=25",
			"62183713=45",
			"62183716=60",
			"62183743=285",
			"62183744=290",
			"62183748=310",
			"6218374A=320",
			"62183754=370",
			"62183756=380",
			"6218375A=400",
			"62183761=435",
			}, delimiter = '=')
	public void parameterizedTest(String input, Integer expected) {
		assertEquals(input, expected);
	}
}
