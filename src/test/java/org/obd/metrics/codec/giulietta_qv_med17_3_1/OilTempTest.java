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


public class OilTempTest implements Giulietta_QV_Med_17_3_1_Test {
	
	@ParameterizedTest
	@CsvSource(value = {
			"62194F2D85=0",
			"62194F2DC5=1",
			"62194F2DA5=1",
			"62194F2E25=3",
	        "62194F2E45=4",
			"62194F2FA5=11",
			"62194F30E5=20",
			"62194F3725=57",
			"62194F3745=58",
			"62194F3CE5=92",
			"62194F3BC5=86",
			"62194F3B85=85",
	        "62194F3E65=99",
	        "62194F3F45=104",
	        "62194F3B65=83"
	         }, delimiter = '=')
	public void parameterizedTest(String input, Float expected) {
		assertCloseTo(input, expected, 3);
	}
}
