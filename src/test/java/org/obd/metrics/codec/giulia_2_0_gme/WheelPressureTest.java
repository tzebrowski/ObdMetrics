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

public class WheelPressureTest implements Giulia_2_0_GME_Test {

	@Override
	public String getPidFile() {
		return "rfhub_module.json";
	}

	@ParameterizedTest
	@CsvSource(value = { "6231D00878=2.168"}, delimiter = '=')
	public void frontLeftWheelTest(String input, double expected) {
		
		assertEquals(input, expected);
	}

	
	@ParameterizedTest
	@CsvSource(value = { "6231D10878=2.168"}, delimiter = '=')
	public void frontRightWheelTest(String input, double expected) {
		
		assertEquals(input, expected);
	}
	
	
	
	@ParameterizedTest
	@CsvSource(value = { "6231D20878=2.168"}, delimiter = '=')
	public void rearLeftWheelTest(String input, double expected) {
		
		assertEquals(input, expected);
	}

	
	@ParameterizedTest
	@CsvSource(value = { "6231D308CB=2.251"}, delimiter = '=')
	public void rearRightWheelTest(String input, double expected) {
		
		assertEquals(input, expected);
	}
}
