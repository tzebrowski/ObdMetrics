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

public class CalculatedAirFlowRateTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"623A600000=0.0",
			"623A60005A=11.25",
			"623A6000B2=22.25",
			"623A600074=14.5",
			"623A6000FF=31.88",
			"623A600FFF=511.88",
			"623A601AFF=863.88",
			}, delimiter = '=')
	public void parameterizedTest(String input, Double expected) {
		assertEquals(input, expected);
	}
}
