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


public class OilTempTest implements Giulietta_QV_Med_17_3_1_Test {
	
	@ParameterizedTest
	@CsvSource(value = {
			"62194F2D00=-3.14",
			"62194F2D85=-0.03",
			"62194F2DA5=0.72",
			"62194F2E25=3.72",
	        "62194F2E45=4.47",
			"62194F2FA5=12.72",
			"62194F30E5=20.22",
			"62194F3725=57.72",
			"62194F3745=58.47",
			"62194F3B65=83.22",
			"62194F3BC5=85.47",
			"62194F3B85=83.97",
			"62194F3CE5=92.22",
			"62194F3E65=101.23",
	        "62194F3F45=106.48",
	        "62194F3FFF=110.83"
	}, delimiter = '=')
	public void parameterizedTest(String input, Double expected) {
		assertEquals(input, expected);
	}
}
