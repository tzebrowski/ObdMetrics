 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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
package org.obd.metrics.codec.dtc;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.dtc.UdsSnapshotParser;
import org.obd.metrics.command.dtc.UdsSnapshotResponse;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;

public class UdsSnapshotParserTest {

	@Test
	public void multiFrameResponseTest() {
							  	
		final String rawData =  "0310:59040115158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final UdsSnapshotParser parser = new UdsSnapshotParser(registry);
		final UdsSnapshotResponse result = parser.parse(rawData);
		
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.isError()).isFalse();
		Assertions.assertThat(result.getErrorMessage()).isEmpty();
		
		System.out.println("Parsed Successfully!");
		System.out.println("DTC: " + result.getDtcHex());
		System.out.println("Number of DIDs: " + result.getNumberOfDids());
		System.out.println("Raw Data to decode later: " + result.getRawDataBlock());

		result.getExtractedDids().forEach( p-> {
			System.out.println(p.getDefinition().getPid() + " : " +  p.getDecodedValue());
		});
	}
}
