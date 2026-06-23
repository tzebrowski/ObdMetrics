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

public class KnockSennsorSignalTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62189100=0.00",
			"6218911D=141.6",
			"621891FF=1245.1",
			"621891AF=854.5",
	}, delimiter = '=')
	public void cyl1(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62189200=0.00",
			"6218921D=141.6",
			"621892FF=1245.1",
			"621892AF=854.5",
	}, delimiter = '=')
	public void cyl2(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62189300=0.00",
			"6218931D=141.6",
			"621893FF=1245.1",
			"621893AF=854.5",
	}, delimiter = '=')
	public void cyl3(String input, Double expected) {
		assertEquals(input, expected);	
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62189400=0.00",
			"6218941D=141.6",
			"621894FF=1245.1",
			"621894AF=854.5",
	}, delimiter = '=')
	public void cyl4(String input, Double expected) {
		assertEquals(input, expected);	
	}
}
