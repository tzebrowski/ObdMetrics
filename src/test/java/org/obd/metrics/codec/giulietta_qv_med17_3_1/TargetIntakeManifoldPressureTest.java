/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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

public class TargetIntakeManifoldPressureTest implements Giulietta_QV_Med_17_3_1_Test {
	@ParameterizedTest
	@CsvSource(value = { 
		"621828000F=1",
		"62182800FF=19",
		"6218280FFF=319",
		"621828214B=665",
		"6218285AFF=1819",
		"621828AFFF=3519",
		"621828FFFF=5119",
			
	}, delimiter = '=')
	public void parameterizedTest(String input, Integer expected) {
		assertEquals(input, expected);
	}
}
