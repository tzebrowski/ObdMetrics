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
package org.obd.metrics.command.dtc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obd.metrics.api.model.DiagnosticTroubleCode;

final class UdsMultiFrameDtcParser {

	public static void main(String[] args) {
		final String multiFrameData = "7F197800B0:5902CF0191111:08C4058108";
		final UdsMultiFrameDtcParser parser = new UdsMultiFrameDtcParser();

		final UdsResponse result = parser.parse(multiFrameData);

		if (result.hasError()) {
			System.out.println("Error: " + result.error);
		} else {
			System.out.println("Reassembled Payload: " + result.rawPayload);
			System.out.println("\nECU Supported Statuses (Mask: 0x" + result.statusAvailabilityMaskHex + "):");
			for (String status : result.supportedStatuses) {
				System.out.println(" - " + status);
			}

			System.out.println("\n--- Extracted DTCs ---");
			for (DiagnosticTroubleCode dtc : result.dtcs) {
				System.out.println(dtc);
			}
		}
	}

	private DtcDictionary dictionary;

	public UdsMultiFrameDtcParser() {
		this(new DefaultDtcDictionary());
	}

	public UdsMultiFrameDtcParser(DtcDictionary dictionary) {
		this.dictionary = dictionary;
	}

	public String extractPayload(String rawMultiFrame) {
		if (rawMultiFrame == null) {
			return "";
		}
		
		String cleaned = rawMultiFrame.replaceAll("\\s+", "");
		cleaned = cleaned.replaceAll("7F[0-9A-F]{2}78", ""); // Strip pending messages

		int expectedBytes = -1;
		final Matcher m = Pattern.compile("^([0-9A-F]{3})0:").matcher(cleaned);
		if (m.find()) {
			expectedBytes = Integer.parseInt(m.group(1), 16);
		}

		String payload = cleaned.replaceAll("(?:^[0-9A-F]{3})?[0-9A-F]:", "");
		if (expectedBytes > 0 && payload.length() >= expectedBytes * 2) {
			payload = payload.substring(0, expectedBytes * 2);
		}
		return payload;
	}

	public UdsResponse parse(String rawMultiFrame) {
		final UdsResponse response = new UdsResponse();
		final String payload = extractPayload(rawMultiFrame);
		response.rawPayload = payload;

		if (payload.isEmpty()) {
			response.error = "Payload is empty after extraction.";
			return response;
		}

		if (!payload.startsWith("5902")) {
			response.error = "Not a valid UDS Service $19 02 positive response. Payload: " + payload;
			return response;
		}

		response.statusAvailabilityMaskHex = payload.substring(4, 6);
		final int maskValue = Integer.parseInt(response.statusAvailabilityMaskHex, 16);
		response.supportedStatuses = decodeStatusBits(maskValue);

		final String dtcData = payload.substring(6);

		for (int i = 0; i < dtcData.length(); i += 8) {
			if (i + 8 <= dtcData.length()) {
				String dtcHex = dtcData.substring(i, i + 6);
				String statusHex = dtcData.substring(i + 6, i + 8);
				response.dtcs.add(decodeUdsDtc(dtcHex, statusHex));
			}
		}
		return response;
	}

	private DiagnosticTroubleCode decodeUdsDtc(String hex3Bytes, String statusHex) {
		final int byte1 = Integer.parseInt(hex3Bytes.substring(0, 2), 16);
		final String byte2 = hex3Bytes.substring(2, 4);
		final String ftbHex = hex3Bytes.substring(4, 6);

		final int systemBits = (byte1 >> 6) & 0x03;
		final char systemChar = "PCBU".charAt(systemBits);

		final int categoryBits = (byte1 >> 4) & 0x03;
		final int subsystemBits = byte1 & 0x0F;

		final String standardCode = String.format("%c%d%X%s", systemChar, categoryBits, subsystemBits, byte2);

		final char sysChar = standardCode.charAt(0);
		final char catChar = standardCode.charAt(1);
		final char subChar = standardCode.charAt(2);

		final String systemDesc = dictionary.getSystem(sysChar, "Unknown System");
		final String categoryDesc = dictionary.getCategory(catChar, "Unknown Category");
		final String subsystemDesc;

		if (sysChar == 'P') {
			subsystemDesc = dictionary.getPowerTrain(subChar, "Unknown Subsystem");
		} else {
			subsystemDesc = "Subsystem index " + subChar + " (System specific)";
		}

		final DtcComponent system = new DtcComponent(String.valueOf(sysChar), systemDesc);
		final DtcComponent category = new DtcComponent(String.valueOf(catChar), categoryDesc);
		final DtcComponent subsystem = new DtcComponent(String.valueOf(subChar), subsystemDesc);

		final int statusMask = Integer.parseInt(statusHex, 16);

		final String ftbDesc = dictionary.getFailureType(ftbHex, "Unknown Subtype");
		final DtcComponent failureType = new DtcComponent(ftbHex, ftbDesc);

		final List<String> statuses = decodeStatusBits(statusMask);
		final String fullDescription = dictionary.getDescription(hex3Bytes);

		return new DiagnosticTroubleCode(standardCode, ftbHex, hex3Bytes, fullDescription, statusMask, statuses, system,
				category, subsystem, failureType);
	}

	private List<String> decodeStatusBits(int status) {
		final List<String> active = new ArrayList<>();
		if ((status & 0x01) != 0)
			active.add("Test Failed");
		if ((status & 0x02) != 0)
			active.add("Test Failed This Operation Cycle");
		if ((status & 0x04) != 0)
			active.add("Pending DTC");
		if ((status & 0x08) != 0)
			active.add("Confirmed DTC");
		if ((status & 0x10) != 0)
			active.add("Test Not Completed Since Last Clear");
		if ((status & 0x20) != 0)
			active.add("Test Failed Since Last Clear");
		if ((status & 0x40) != 0)
			active.add("Test Not Completed This Operation Cycle");
		if ((status & 0x80) != 0)
			active.add("Warning Indicator Requested");
		return active;
	}
}