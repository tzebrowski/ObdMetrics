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

public class OilTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"621302000B=11",
			"6213020022=34",
			"621302003E=62",
			"6213020040=64",
			"6213020043=67",
			"6213020055=85",
			"6213020056=86",
			"6213020060=96",
			"621302FFF0=-16"
			}, delimiter = '=')
	public void parameterizedTest(String input, Integer expected) {
		assertEquals(Boolean.TRUE, input, expected);
	}
}
