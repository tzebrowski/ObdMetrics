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
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class VinCommandTest {
	
	
	@ParameterizedTest
	@CsvSource(value = { 
		"0140:4902015756571:5A5A5A314B5A412:4D363930333932;WVWZZZ1KZAM690392",//correct
		"0140:4802015756571:5A5A5A314B5A412:4D363930333932;", // incorrect success code
		"0140:4902015756571:5A5A5A314B5A412:4D363930333;", // incorrect hex
	}, delimiter = ';')
	public void hexCommandTest(String raw,String expected) {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(11000l));
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(raw.getBytes()));
		Assertions.assertThat(decode).isEqualTo(expected);
	}
}
