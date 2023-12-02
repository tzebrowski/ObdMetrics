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
package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Disabled
public class WheelTempTest implements Giulia_2_0_GME_Test {

	@Override
	public String getPidFile() {
		return "rthub_module.json";
	}

	@ParameterizedTest
	@CsvSource(value = { "00B0:6240B10878001:003E0183DCAAAA=12"}, delimiter = '=')
	public void frontLeftWheelTest(String input, double expected) {
		//02BF - 703
		assertEquals(150l, input, expected);
	}

	
	@ParameterizedTest
	@CsvSource(value = { "00B0:6240B20878001:003B0182DCAAAA=9"}, delimiter = '=')
	public void frontRightWheelTest(String input, double expected) {
		//02BF - 703
		assertEquals(151l, input, expected);
	}
	
	
	
	@ParameterizedTest
	@CsvSource(value = { "00B0:6240B40878001:00380183DCAAAA=9"}, delimiter = '=')
	public void rearLeftWheelTest(String input, double expected) {
		//02BF - 703
		assertEquals(153l, input, expected);
	}

	
	@ParameterizedTest
	@CsvSource(value = { "00B0:6240B308CB001:003B0183DCAAAA=6"}, delimiter = '=')
	public void rearRightWheelTest(String input, double expected) {
		//02BF - 703
		assertEquals(152l, input, expected);
	}
	
}
