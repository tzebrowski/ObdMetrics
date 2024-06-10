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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class LambdaSignalTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6218980000=-1",
			"621898000F=-0.927",
			"62189800FF=0.245",
			"6218980FFF=18.995",
			
	}, delimiter = '=')
	public void lambda1SignalPreCat(String input, Double expected) {
		assertEquals(input, expected);
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62189A0000=-1",
			"62189A000F=-0.927",
			"62189A00FF=0.245",
			"62189A0FFF=18.995",
			
	}, delimiter = '=')
	public void lambda1SignalPostCat(String input, Double expected) {
		assertEquals(input, expected);
	}

}
