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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.command.meta.NotEncodedCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class MetadataDecoderTest {
	
	@Test
	public void ecuIsoCodeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(16002l));
		
		String answer = "0080:62F1A50807191:8986";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0807198986");
	}
	
	@Test
	public void ecuSofwareNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(16003l));
		
		String answer = "00E0:62F1943130331:373532393935312:20";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("1037529951");
	}
	
	
	@Test
	public void ecuTypeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(16005l));
		
		String answer = "00E0:62F1923032361:315330353631382:20";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0261S05618");
	}
	
	@Test
	public void sparePartNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(16006l));
		
		String answer = "00E0:62F1873535321:353030373220202:20";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("55250072");
	}
	
	
	@Test
	public void homologationNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(16007l));
		
		String answer = "0090:62F1964431371:334530";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("D173E0");
	}
	
	@Test
	public void softwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(16008l));
		
		String answer = "62F1950406";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0406");
	}
	
	
	@Test
	public void hardwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("alfa.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(16009l));
		
		String answer = "62F19300";
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("00");
	}
}
