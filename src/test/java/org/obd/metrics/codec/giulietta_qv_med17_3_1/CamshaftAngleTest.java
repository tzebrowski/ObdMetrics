 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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

public class CamshaftAngleTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62196C0000=00",
			"62196C0A00=20",
			"62196C09D0=19.60",
			"62196C0FFF=32.0",
			"62196CF001=-32"
	}, delimiter = '=')
	public void target(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62196D0000=00",
			"62196D0A11=20.1",
			"62196D09D0=19.6",
			"62196D0FFF=32.0",
			"62196DFFF2=-0.1",
			"62196D0A24=20.3",
			"62196DFF11=-1.9",
			"62196DFF99=-0.80",
			"62196DFF99=-0.80",
			"62196DEE99=-34.8",
			"62196DDD99=-68.8",
			"62196DCCFF=-102",
			"62196DBBFF=-136",
			"62196DAAFF=-170",
			"62196D99FF=-204",
			"62196D88FF=-238",
			"62196D77FF=240",
			}, delimiter = '=')
	public void measured(String input, Double expected) {
		assertEquals(input, expected);
	}
}
