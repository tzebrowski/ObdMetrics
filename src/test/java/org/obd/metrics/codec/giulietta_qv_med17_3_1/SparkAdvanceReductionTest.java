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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SparkAdvanceReductionTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62186C00=0",
			"62186C0A=7.5",
			"62186C0F=11.25",
			"62186C10=12.0",
			"62186C2F=35.25",
			"62186C5F=71.25"
			
	}, delimiter = '=')
	public void cylinder1Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	

	@ParameterizedTest
	@CsvSource(value = { 
			"62186D00=0",
			"62186D0A=7.5",
			"62186D0F=11.25",
			"62186D10=12.0",
			"62186D2F=35.25",
			"62186D5F=71.25"
			
	}, delimiter = '=')
	public void cylinder2Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62186E00=0",
			"62186E0A=7.5",
			"62186E0F=11.25",
			"62186E10=12.0",
			"62186E2F=35.25",
			"62186E5F=71.25"
			
	}, delimiter = '=')
	public void cylinder3Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62186F00=0",
			"62186F0A=7.5",
			"62186F0F=11.25",
			"62186F10=12.0",
			"62186F2F=35.25",
			"62186F5F=71.25"
			
	}, delimiter = '=')
	public void cylinder4Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
}
