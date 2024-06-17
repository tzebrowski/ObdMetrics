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

public class MisifresTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6220510001=1",
			"622051000F=15",
			"6220510FFF=4095",
			"622051FFFF=65535"
	}, delimiter = '=')
	public void cylinder1Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	

	@ParameterizedTest
	@CsvSource(value = { 
			"6220520001=1",
			"622052000F=15",
			"6220520FFF=4095",
			"622052FFFF=65535"
			
	}, delimiter = '=')
	public void cylinder2Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6220530001=1",
			"622053000F=15",
			"6220530FFF=4095",
			"622053FFFF=65535"
			
	}, delimiter = '=')
	public void cylinder3Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6220540001=1",
			"622054000F=15",
			"6220540FFF=4095",
			"622054FFFF=65535"
			
	}, delimiter = '=')
	public void cylinder4Test(String input, Double expected) {
		assertEquals(input, expected);
	}
	
}
