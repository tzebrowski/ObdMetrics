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
package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UniAirOilTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = {
			"62198E04EA=38.63",
			"62198E075C=77.75",
			"62198E07A8=82.50",
			"62198E07C9=84.50",
			"62198E0323=10.19",
			"62198E034B=12.69",
			"62198E07DB=85.19",
			"62198E0861=89.0",
			"62198E0649=67.0"
			}, delimiter = '=')
	public void parameterizedTest(String input, Float expected) {
		assertCloseTo(input, expected, 2.0f);
	}
}
