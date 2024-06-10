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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class KnockSensorSignalTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6218410000=00",
			"62184100A0=20.00",
			"6218410AA0=340.00",
			"6218410CDB=411.38",
			"621841FFDB=8187.38"
			}, delimiter = '=')
	public void parameterizedTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218910000=00",
			"62189100A0=20.00",
			"6218910AA0=340.00",
			"6218910CDB=411.38",
			"621891FFDB=8187.38"
			}, delimiter = '=')
	public void cylinder1Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218920000=00",
			"62189200A0=20.00",
			"6218920AA0=340.00",
			"6218920CDB=411.38",
			"621892FFDB=8187.38"
			}, delimiter = '=')
	public void cylinder2Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218930000=00",
			"62189300A0=20.00",
			"6218930AA0=340.00",
			"6218930CDB=411.38",
			"621893FFDB=8187.38"
			}, delimiter = '=')
	public void cylinder3Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218940000=00",
			"62189400A0=20.00",
			"6218940AA0=340.00",
			"6218940CDB=411.38",
			"621894FFDB=8187.38"
			}, delimiter = '=')
	public void cylinder4Test(String input, Double expected) {
		assertEquals(input, expected);
	}
}
