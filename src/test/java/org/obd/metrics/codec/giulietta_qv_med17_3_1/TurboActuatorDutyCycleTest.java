/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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
package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TurboActuatorDutyCycleTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6218210000=0",
			"621821000F=0.02",
			"62182100FF=0.39",
			"6218210FFF=6.25",
			"62182135FF=21.09",
			"6218215FFF=37.50",
			"621821FFFF=100"
			
	}, delimiter = '=')
	public void parameterizedTest(String input, Double expected) {
		assertEquals(input, expected);
	}
}
