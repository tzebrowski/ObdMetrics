 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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
package org.obd.metrics.codec.giulia_2_0_gme;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.meta.HexCodec;
import org.obd.metrics.command.meta.NotEncodedCodec;
import org.obd.metrics.command.meta.TimeCodec;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class MetadataDecoderTest {
	@Test
	public void vin_1_Test() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "0140:4902015756571:5A5A5A314B5A412:4D363930333932";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(11000l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("WVWZZZ1KZAM690392");
	}
	
	@Test
	public void vin_2_Test() {
		
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "0140:62F1905A41521:454145424E394B2:37363137323839";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17001l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("ZAREAEBN9K7617289");
	}
	
	@Test
	public void ecuSerialNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "0120:62F18C5444341:313930393539452:3031343430";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17002l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("TD4190959E01440");
	}
	
	@Test
	public void ecuSofwareNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "00E0:62F1945031341:315641304520202:20";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17003l), 
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("P141VA0E");
	}
	
	
	@Test
	public void fiatDrawingNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "00E0:62F1913532301:353533323020202:20";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17004l), 
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("52055320");
	}
	
	@Test
	public void ecuTypeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "00E0:62F1924D4D311:304A41485732332:32";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17005l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("MM10JAHW232");
	}
	
	@Test
	public void sparePartNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "00E0:62F1873530351:353938353220202:20";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17006l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("50559852");
	}
	
	
	@Test
	public void homologationNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		HexCodec metadataDecoder = new HexCodec();
		
		String answer = "0090:62F1964548411:423030";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17007l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("EHAB00");
	}
		
	@Test
	public void softwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		NotEncodedCodec metadataDecoder = new NotEncodedCodec();
		
		String answer = "62F1950000";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17008l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0000");
	}

	@Test
	public void hardwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		NotEncodedCodec metadataDecoder = new NotEncodedCodec();
		
		String answer = "62F19300";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17009l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("00");
	}
	
	@Test
	public void ecuIsoCodeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		NotEncodedCodec metadataDecoder = new NotEncodedCodec();
		
		String answer = "0080:62F1A50001501:7517";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17010l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0001507517");
	}
	
	
	@Test
	public void operatingTimeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		TimeCodec metadataDecoder = new TimeCodec();
		
		String answer = "6210080000BFC8";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17012l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("49096");
	}
	
	@Test
	public void functioningTimeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		TimeCodec metadataDecoder = new TimeCodec();
		
		String answer = "6220080000BFC7";
		String decode = metadataDecoder.decode(pidDefinitionRegistry.findBy(17011l),
				ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("49095");
	}
}
