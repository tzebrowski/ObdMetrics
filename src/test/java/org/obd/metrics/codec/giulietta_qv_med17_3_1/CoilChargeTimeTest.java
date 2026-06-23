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
package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CoilChargeTimeTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62183100=0.0000",
			"621831FF=0.2040",
			"621831AF=0.1400",
	}, delimiter = '=')
	public void coil1(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62183200=0.0000",
			"621832FF=0.2040",
			"621832AF=0.1400",
	}, delimiter = '=')
	public void coil2(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62183300=0.0000",
			"621833FF=0.2040",
			"621833AF=0.1400",
	}, delimiter = '=')
	public void coil3(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62183400=0.0000",
			"621834FF=0.2040",
			"621834AF=0.1400",
	}, delimiter = '=')
	public void coil4(String input, Double expected) {
		assertEquals(input, expected);	
	}
}
