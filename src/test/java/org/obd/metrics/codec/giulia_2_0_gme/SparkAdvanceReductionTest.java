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

public class SparkAdvanceReductionTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62186C0000=0.0",
			"62186C00AA=10.63",
			"62186C0111=17.06",
			"62186C0AAA=170.63",
			"62186C0B91=185.06",
			"62186C1000=256.0",
			"62186CFF00=-16.0"
			}, delimiter = '=')
	public void cylinder1(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62186D0000=0.0",
			"62186D00AA=10.63",
			"62186D0111=17.06",
			"62186D0AAA=170.63",
			"62186D0B91=185.06",
			"62186D1000=256.0",
			"62186DFF00=-16.0"
			}, delimiter = '=')
	public void cylinder2(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62186F0000=0.0",
			"62186F00AA=10.63",
			"62186F0111=17.06",
			"62186F0AAA=170.63",
			"62186F0B91=185.06",
			"62186F1000=256.0",
			"62186FFF00=-16.0"
			}, delimiter = '=')
	public void cylinder4(String input, Double expected) {
		assertEquals(input, expected);
	}
	

	@ParameterizedTest
	@CsvSource(value = { 
			"62186E0000=0.0",
			"62186E00AA=10.63",
			"62186E0111=17.06",
			"62186E0AAA=170.63",
			"62186E0B91=185.06",
			"62186E1000=256.0",
			"62186EFF00=-16.0"
			}, delimiter = '=')
	public void cylinder3(String input, Double expected) {
		assertEquals(input, expected);
	}

}
