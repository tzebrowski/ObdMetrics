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
package org.obd.metrics.codec.batch.decoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class IdGeneratorTest {

	/**
	 * Helper method to dynamically calculate the expected bitwise hash.
	 * This mirrors the logic in the new bitwise IdGenerator.
	 */
	private long expectedHash(long pidId, String payload) {
		long hash = pidId << 32;
		for (char c : payload.toCharArray()) {
			hash = (hash << 8) | (c & 0xFF);
		}
		return hash;
	}

	/**
	 * Helper to dynamically extract the expected raw string without multi-frame colons.
	 * Mirrors the stepping logic of IdGenerator so we can establish a baseline.
	 */
	private String extractCleanPayload(String raw, int pos, int length) {
		StringBuilder sb = new StringBuilder();
		int tokensRead = 0;
		int currentPos = pos;
		
		while (tokensRead < length && currentPos + 1 < raw.length()) {
			// Skip the colon delimiter (e.g., "1:")
			if (raw.charAt(currentPos + 1) == ':') {
				currentPos += 2;
				if (currentPos + 1 >= raw.length()) {
					break;
				}
			}
			sb.append(raw.charAt(currentPos));
			sb.append(raw.charAt(currentPos + 1));
			
			currentPos += 2;
			tokensRead++;
		}
		return sb.toString();
	}

	@Test
	public void arrayIndexOutOfBoundException_Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("FF".getBytes());
		long expected = expectedHash(17L, "FF");
		
		Assertions.assertThat(IdGenerator.generate(1, 17L, 0, bytes)).isEqualTo(expected);
		Assertions.assertThat(IdGenerator.generate(2, 17L, 0, bytes)).isEqualTo(expected);
		Assertions.assertThat(IdGenerator.generate(3, 17L, 0, bytes)).isEqualTo(expected);
		Assertions.assertThat(IdGenerator.generate(4, 17L, 0, bytes)).isEqualTo(expected);
	}

	@Test
	public void shouldHitUnrolledBranch_Length1() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDD".getBytes());
		long expected = expectedHash(17L, "AA");
		
		long generated = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(generated).isEqualTo(expected);
	}

	@Test
	public void shouldHitUnrolledBranch_Length2() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDD".getBytes());
		long expected = expectedHash(17L, "AABB");
		
		long generated = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(generated).isEqualTo(expected);
	}

	@Test
	public void shouldHitUnrolledBranch_Length3() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDD".getBytes());
		long expected = expectedHash(17L, "AABBCC");
		
		long generated = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(generated).isEqualTo(expected);
	}

	@Test
	public void shouldHitUnrolledBranch_Length4() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDDEE".getBytes());
		long expected = expectedHash(17L, "AABBCCDD");
		
		long generated = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(generated).isEqualTo(expected);
	}

	@Test
	public void shouldHitFallbackLoop_Length5AndAbove() {
		// 5 tokens = 10 characters
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDDEEFF".getBytes());
		long expectedLength5 = expectedHash(17L, "AABBCCDDEE");
		long expectedLength6 = expectedHash(17L, "AABBCCDDEEFF");
		
		long generated5 = IdGenerator.generate(5, 17L, 0, bytes);
		Assertions.assertThat(generated5).isEqualTo(expectedLength5);

		long generated6 = IdGenerator.generate(6, 17L, 0, bytes);
		Assertions.assertThat(generated6).isEqualTo(expectedLength6);
	}

	@Test
	public void shouldSkipColonsInsideUnrolledBranches() {
		// Payloads with multi-frame delimiters injected at different token intervals
		ConnectorResponse colonAtToken2 = ConnectorResponseFactory.wrap("AA1:BBCCDD".getBytes());
		ConnectorResponse colonAtToken3 = ConnectorResponseFactory.wrap("AABB1:CCDD".getBytes());
		ConnectorResponse colonAtToken4 = ConnectorResponseFactory.wrap("AABBCC1:DD".getBytes());

		// Test Length 2 branch skipping colon
		Assertions.assertThat(IdGenerator.generate(2, 17L, 0, colonAtToken2))
				.describedAs("Length 2 failed to skip colon")
				.isEqualTo(expectedHash(17L, "AABB"));

		// Test Length 3 branch skipping colon
		Assertions.assertThat(IdGenerator.generate(3, 17L, 0, colonAtToken3))
				.describedAs("Length 3 failed to skip colon")
				.isEqualTo(expectedHash(17L, "AABBCC"));

		// Test Length 4 branch skipping colon
		Assertions.assertThat(IdGenerator.generate(4, 17L, 0, colonAtToken4))
				.describedAs("Length 4 failed to skip colon")
				.isEqualTo(expectedHash(17L, "AABBCCDD"));
	}

	@Test
	public void shouldSkipColonsInsideFallbackLoop() {
		// Payload with 4 tokens, then a delimiter, then the 5th and 6th tokens
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("AABBCCDD1:EEFF".getBytes());
		
		long expected = expectedHash(17L, "AABBCCDDEEFF");
		
		long generated = IdGenerator.generate(6, 17L, 0, bytes);
		Assertions.assertThat(generated).isEqualTo(expected);
	}
	@Test
	public void shouldSkipMultiFrameColonDelimiters() {
		ConnectorResponse singleFrame = ConnectorResponseFactory.wrap("00FFDC".getBytes());
		ConnectorResponse multiFrame = ConnectorResponseFactory.wrap("001:FFDC".getBytes());

		long singleFrameId = IdGenerator.generate(3, 17L, 0, singleFrame);
		long multiFrameId = IdGenerator.generate(3, 17L, 0, multiFrame);

		Assertions.assertThat(multiFrameId).isEqualTo(singleFrameId);
		Assertions.assertThat(multiFrameId).isEqualTo(expectedHash(17L, "00FFDC"));
	}

	@Test
	public void shouldGenerateCorrectAndUniqueIdsForMultipleLargeMultiFrameMessages() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		
		List<String> payloads = Arrays.asList(
			"7F22780550:6210005608191:24E2B9186B549B2:182710821828BA3:341937EBA3181F4:FFC9180ED652185:674B70186C8F706:186D592F186EA67:66186FF28610028:B63C18AD011D189:AE819618C7460EA:18AF237A18C840B:29191082E31911C:0E02",
			"7F22780550:621000C9E5191:24E612186BF1082:18270BC81828513:42193715ED181F4:3EC0180E8FD0185:679616186C91546:186DEFB3186EA37:F6186F65A410028:913218AD530E189:AE8D8F18C7A634A:18AFCC5B18C8C9B:6C19108CEC1911C:4E71",
			"7F22780550:6210005EFE191:245A83186B1FB62:1827EF201828803:3C19378EB9181F4:CC10180E06A0185:676DB0186CAE4B6:186D6ED6186E927:DE186FD0CD10028:AEE418ADFAC2189:AEC2F418C7A73EA:18AFECBA18C84EB:3C19104C031911C:E00D",
			"7F22780550:621000408C191:244E5D186BD3EB2:182768901828833:8B19374E3E181F4:F486180EAF91185:67D43D186CD1066:186DC483186E607:87186F6D0610028:3AFD18AD79F0189:AEB74318C73EA6A:18AFD7EA18C840B:9F1910CFEF1911C:D05D",
			"7F22780550:621000EE14191:241527186BBFA92:1827A17C1828613:401937CCB7181F4:11A3180EBCDB185:67C3DD186CF20F6:186D7FC6186E647:E6186F7A6A10028:B96C18ADCD5C189:AE3E1918C78BAAA:18AF45B118C89BB:BF191094A41911C:94B2",
			"7F22780550:621000E683191:249C39186BD1E82:1827739818285B3:851937ADAF181F4:4084180E66EF185:6719BC186C38CB6:186DE4FE186E587:5B186FA95010028:C67618ADEA22189:AEEE3818C7B2AEA:18AF103C18C8F0B:DC191064971911C:5116",
			"7F22780550:6210009DD4191:242290186B58632:1827BACF18285C3:AE1937D273181F4:E761180E2FB2185:677B30186CEB1F6:186D81E9186E3C7:2C186F097E10028:7F6318ADBBFB189:AEAB4018C76FCAA:18AF623018C8A7B:8B1910749A1911C:28B4",
			"7F22780550:62100006BD191:243B92186B92F92:18270CB71828893:0E1937E006181F4:2305180E5586185:67BC87186CBB486:186D364D186E797:33186FFB0C10028:1DA218AD3F60189:AE7CE618C742DBA:18AFE2FF18C812B:121910070D1911C:17C5",
			"7F22780550:62100087DF191:244B35186B2FDA2:1827C1E41828653:A81937F9E4181F4:C180180E475D185:678DD6186C0FF16:186DDBB2186EF77:84186F649510028:B4A518AD5E1C189:AEDDEB18C7A15BA:18AF709B18C80DB:02191067AE1911C:9643",
			"7F22780550:6210008E1C191:2416D3186B35542:1827064D1828A03:EC1937488C181F4:53E5180E08A8185:6792B2186CC8FB6:186DA773186EC07:63186F39C410028:A08518AD225F189:AE0FD118C72D1DA:18AF8D2318C89AB:C419105C591911C:123A",
			"7F22780550:6210005981191:24B7AC186B765C2:1827D0CB18288B3:BA193734A1181F4:F7D5180E63BB185:677C64186C49776:186D349F186E4A7:9B186F75D110028:BFAC18AD5302189:AE562918C7A284A:18AF587118C811B:05191027951911C:37B1",
			"7F22780550:621000D9E0191:24CA55186B43822:18278E741828FE3:7F1937C77A181F4:BA65180E510C185:671B8B186C08B96:186DBC59186E7F7:26186FF45010028:6B2A18AD623B189:AE376C18C74980A:18AFDFF918C8A5B:DB19103ECA1911C:F8D2",
			"7F22780550:621000F73B191:24D805186B6CD32:18278A5318288B3:F41937D9F1181F4:61AC180E8502185:67A12C186C8DAD6:186DD857186E057:F0186F7A9210028:652018AD1EE4189:AE3B1918C7A47BA:18AF550018C8DCB:6C1910AEA31911C:42C7"
		);

		// Layout definition for the new response format: {PID, Expected String Position, Token Length}
		Object[][] frameMapping = {
			{"1000", 17, 2}, {"1924", 27, 2}, {"186B", 35, 2}, {"1827", 45, 2},
			{"1828", 53, 2}, {"1937", 63, 2}, {"181F", 73, 2}, {"180E", 81, 2},
			{"1867", 91, 2}, {"186C", 99, 2}, {"186D", 109, 2}, {"186E", 117, 2},
			{"186F", 125, 2}, {"1002", 137, 2}, {"18AD", 145, 2}, {"18AE", 155, 2},
			{"18C7", 163, 2}, {"18AF", 173, 2}, {"18C8", 181, 2}, {"1910", 189, 2},
			{"1911", 199, 2}
		};

		Set<Long> allGeneratedIds = new HashSet<>();

		for (String payload : payloads) {
			ConnectorResponse bytes = ConnectorResponseFactory.wrap(payload.getBytes());
			
			for (Object[] spec : frameMapping) {
				String pid = (String) spec[0];
				int pos = (Integer) spec[1];
				int length = (Integer) spec[2];
				
				long pidId = registry.findBy(pid).getId();
				long generatedId = IdGenerator.generate(length, pidId, pos, bytes);
				
				// Automatically determine what the pure hex string is by actively stripping colons
				String cleanPayload = extractCleanPayload(payload, pos, length);
				
				// Assert the Generator logically matches standard bitwise hashing without colons
				Assertions.assertThat(generatedId)
					.describedAs("Mismatch for PID %s in payload %s", pid, payload)
					.isEqualTo(expectedHash(pidId, cleanPayload));
				
				allGeneratedIds.add(generatedId);
			}
		}
		
		// Assert we successfully extracted and hashed a wide variety of unique values 
		// (13 distinct payloads * 21 PIDs = 273 extractions. Expect > 250 unique values).
		Assertions.assertThat(allGeneratedIds.size()).isEqualByComparingTo(271);
	}
}