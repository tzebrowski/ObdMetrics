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

	// rkrnv6_w is a 16-bit word (5/1024 V per count), sent high byte first: 2 bytes, not 1.

	@ParameterizedTest
	@CsvSource(value = {
			"6218910000=0.0",
			"621891001D=0.142",
			"6218911D00=36.25",
			"6218912800=50.0",
			"6218911D2A=36.455",
			"621891FFFF=319.994",
	}, delimiter = '=')
	public void cyl1(String input, Double expected) {
		assertEquals(input, expected);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"6218920000=0.0",
			"621892001D=0.142",
			"6218921D00=36.25",
			"6218922800=50.0",
			"6218921D2A=36.455",
			"621892FFFF=319.994",
	}, delimiter = '=')
	public void cyl2(String input, Double expected) {
		assertEquals(input, expected);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"6218930000=0.0",
			"621893001D=0.142",
			"6218931D00=36.25",
			"6218932800=50.0",
			"6218931D2A=36.455",
			"621893FFFF=319.994",
	}, delimiter = '=')
	public void cyl3(String input, Double expected) {
		assertEquals(input, expected);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"6218940000=0.0",
			"621894001D=0.142",
			"6218941D00=36.25",
			"6218942800=50.0",
			"6218941D2A=36.455",
			"621894FFFF=319.994",
	}, delimiter = '=')
	public void cyl4(String input, Double expected) {
		assertEquals(input, expected);
	}
}
