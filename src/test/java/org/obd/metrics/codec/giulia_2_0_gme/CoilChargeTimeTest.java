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

public class CoilChargeTimeTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6218310000=0.0",
			"62183100A0=0.1280",
			"6218310AA0=2.1760",
			"6218310AAA=2.1840",
			"621831AAAA=34.9520",
			
			}, delimiter = '=')
	public void coil1Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218320000=0.0",
			"62183200A0=0.1280",
			"6218320AA0=2.1760",
			"6218320AAA=2.1840",
			"621832AAAA=34.9520",
			}, delimiter = '=')
	public void coil2Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218330000=0.0",
			"62183300A0=0.1280",
			"6218330AA0=2.1760",
			"6218330AAA=2.1840",
			"621833AAAA=34.9520",
			}, delimiter = '=')
	public void coil3Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218340000=0.0",
			"62183400A0=0.1280",
			"6218340AA0=2.1760",
			"6218340AAA=2.1840",
			"621834AAAA=34.9520",
			}, delimiter = '=')
	public void coil4Test(String input, Double expected) {
		assertEquals(input, expected);
	}
}
