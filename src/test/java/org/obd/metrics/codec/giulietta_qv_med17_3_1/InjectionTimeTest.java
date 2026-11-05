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

public class InjectionTimeTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			// DID 18A6 reads ti_l: 32-bit, 0.001 ms per bit (D1739209G0 answers 4 bytes; 0 on this direct-injection build)
			"6218A600000000=0",
			"6218A60000000F=0.015",
			"6218A6000000FF=0.255",
			"6218A600000FFF=4.095",
			"6218A60000FFFF=65.535",
			"6218A600001705=5.893",
			"6218A600001A00=6.656",
			"6218A60001000A=65.546",
	}, delimiter = '=')
	public void parameterizedTest(String input, Double expected) {
		assertEquals(input, expected);
	}
}
