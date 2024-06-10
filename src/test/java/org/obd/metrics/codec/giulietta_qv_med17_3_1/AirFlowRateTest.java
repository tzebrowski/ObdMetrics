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
package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AirFlowRateTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62182F0000=0.0",
			"62182F0055=0.85",
			"62182F0A55=26.45",
			"62182F0D55=34.13",
			"62182F0F11=38.57",
			"62182FFFFF=655.35"
			}, delimiter = '=')
	public void measuredAirFlowRateTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218300000=0.0",
			"6218300055=0.85",
			"6218300A55=26.45",
			"6218300D55=34.13",
			"6218300F11=38.57",
			"621830FFFFF=655.35"
			}, delimiter = '=')
	public void targetAirFlowRateTest(String input, Double expected) {
		assertEquals(input, expected);
	}
}
