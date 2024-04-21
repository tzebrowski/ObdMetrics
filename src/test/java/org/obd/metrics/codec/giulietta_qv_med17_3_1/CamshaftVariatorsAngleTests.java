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

public class CamshaftVariatorsAngleTests implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62181C0000=00",
			"62181C0A00=20",
			"62181C09D0=19.60",
			"62181C0FFF=32.0"
	}, delimiter = '=')
	public void intakePhaseVariatorAngleTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62181B0000=00",
			"62181B0A00=20",
			"62181B09D0=19.60",
			"62181B0FFF=32.0"
	}, delimiter = '=')
	public void targetIntakePhaseVariatorAngleTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62383E0000=00",
			"62383E0A00=20",
			"62383E09D0=19.60",
			"62383E0FFF=32.0"
	}, delimiter = '=')
	public void exhaustPhaseVariatorAngleTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62383D0000=00",
			"62383D0A00=20",
			"62383D09D0=19.60",
			"62383D0FFF=32.0"
	}, delimiter = '=')
	public void targetExhaustPhaseVariatorAngleTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62196E0000=00",
			"62196E0A00=20",
			"62196E09D0=19.60",
			"62196E0FFF=32.0"
	}, delimiter = '=')
	public void overlapAngleTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62197B0000=00",
			"62197B0A00=20",
			"62197B09D0=19.60",
			"62197B0FFF=32.0"
	}, delimiter = '=')
	public void intakePhaseVariatorPositionTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62197D0000=00",
			"62197D0A00=20",
			"62197D09D0=19.60",
			"62197D0FFF=32.0"
	}, delimiter = '=')
	public void targetIntakePhaseVariatorPositionTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62197A0000=00",
			"62197A0A00=20",
			"62197A09D0=19.60",
			"62197A0FFF=32.0"
	}, delimiter = '=')
	public void exhaustPhaseVariatorPositionTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62197C0000=00",
			"62197C0A00=20",
			"62197C09D0=19.60",
			"62197C0FFF=32.0"
	}, delimiter = '=')
	public void targetExhaustPhaseVariatorPositionTest(String input, Double expected) {
		assertEquals(input, expected);
	}
	
}
