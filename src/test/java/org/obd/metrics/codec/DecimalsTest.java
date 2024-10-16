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
package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class DecimalsTest {

	@ParameterizedTest
	@CsvSource(value = { 
		"139;8b",
		"90;5a",
	}, delimiter = ';')
	void decimalTest(String actual, String expected) {
		Assertions.assertThat(Integer.valueOf(actual)).isEqualTo(Integer.parseInt(expected, 16));
		
		ConnectorResponse wrap = ConnectorResponseFactory.wrap(expected.getBytes());
		Assertions.assertThat(Integer.valueOf(actual)).isEqualTo(wrap.getUnsignedBy(0));
	}
}
