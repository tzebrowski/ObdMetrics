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
import org.obd.metrics.translation.TranslationProvider;

public final class UdsMultiFrameDtcParser {

	private DtcDictionary dictionary;
	private TranslationProvider translationProvider;

	public UdsMultiFrameDtcParser() {
		this(new DefaultDtcDictionary(), TranslationProvider.NOOP);
	}

	public UdsMultiFrameDtcParser(DtcDictionary dictionary, TranslationProvider translationProvider) {
		this.dictionary = dictionary;
		this.translationProvider = translationProvider;
	}

	public UdsMultiFrameDtcParser(TranslationProvider translationProvider) {
		this(new DefaultDtcDictionary(translationProvider), translationProvider);
	}

	public String extractPayload(String rawMultiFrame) {
		if (rawMultiFrame == null) {
			return "";
		}

		// Only strip pending messages (NRC 78) if they appear at the very start 
		// of the data stream to avoid corrupting valid DTCs in the middle of the payload.
		final String cleaned = rawMultiFrame.replaceAll("\\s+", "").replaceFirst("^(?:7F[0-9A-F]{2}78)+", "");
		

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
		response.setRawPayload(payload);

		if (payload.isEmpty()) {
			response.setErrorMessage("Payload is empty after extraction.");
			return response;
		}

		if (!payload.startsWith("5902")) {
			response.setErrorMessage("Not a valid UDS Service $19 02 positive response. Payload: " + payload);
			return response;
		}

		// Safely check payload length before extracting the status mask
		if (payload.length() < 6) {
			response.setErrorMessage("Payload too short to contain Status Availability Mask.");
			return response;
		}

		response.setStatusAvailabilityMaskHex(payload.substring(4, 6));
		final int maskValue = Integer.parseInt(response.getStatusAvailabilityMaskHex(), 16);
		response.setSupportedStatuses(decodeStatusBits(maskValue));

		final String dtcData = payload.substring(6);

		for (int i = 0; i < dtcData.length(); i += 8) {
			if (i + 8 <= dtcData.length()) {
				final String dtcHex = dtcData.substring(i, i + 6);
				final String statusHex = dtcData.substring(i + 6, i + 8);
				
				// Protection against ISO-TP padding bytes (0x00 or 0xAA) being 
				// read as DTCs in case expectedBytes extraction failed.
				if ((dtcHex.equals("000000") && statusHex.equals("00")) || 
				    (dtcHex.equals("AAAAAA") && statusHex.equals("AA"))) {
					break; 
				}
				response.addDiagnosticTroubleCode(decodeUdsDtc(dtcHex, statusHex)); 
			}
		}
		return response;
	}

	
	private DiagnosticTroubleCode decodeUdsDtc(String hex3Bytes, String statusHex) {
	    final int dtcValue = Integer.parseInt(hex3Bytes, 16);
	    final int byte1 = (dtcValue >> 16) & 0xFF;
	    final int byte2 = (dtcValue >> 8) & 0xFF;
	    final int ftb = dtcValue & 0xFF;

	    final int systemBits = (byte1 >> 6) & 0x03;
	    final char sysChar = "PCBU".charAt(systemBits);
	    
	    final int categoryBits = (byte1 >> 4) & 0x03;
	    final char catChar = Character.forDigit(categoryBits, 10);
	    
	    final int subsystemBits = byte1 & 0x0F;
	    final char subChar = Character.toUpperCase(Character.forDigit(subsystemBits, 16));

	    // Formats byte2 directly to a 2-character uppercase hex string
	    final String b2Hex = String.format("%02X", byte2);
	    final String standardCode = "" + sysChar + catChar + subChar + b2Hex;

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

	    // Formats the FTB safely to a 2-character uppercase hex string
	    final String ftbHex = String.format("%02X", ftb);
	    final String ftbDesc = dictionary.getFailureType(ftbHex, "Unknown Subtype");
	    final DtcComponent failureType = new DtcComponent(ftbHex, ftbDesc);

	    final List<String> statuses = decodeStatusBits(statusMask);
	    String fullDescription = dictionary.getDescription(hex3Bytes);

	    if (DtcDictionary.UNKNOWN_DTC.equals(fullDescription)) {
	    	//fallback
	        fullDescription = dictionary.getDtcDescription(standardCode, fullDescription);
	    }
	    
	    return new DiagnosticTroubleCode(standardCode, ftbHex, hex3Bytes, fullDescription, statusMask, statuses, system,
	            category, subsystem, failureType);
	}

	private List<String> decodeStatusBits(int status) {
		final List<String> active = new ArrayList<>(8);

		if ((status & 0x01) != 0) active.add(translationProvider.translateDtcStatusBit("Test Failed", "Test Failed"));
		if ((status & 0x02) != 0) active.add(translationProvider.translateDtcStatusBit("Test Failed This Operation Cycle", "Test Failed This Operation Cycle"));
		if ((status & 0x04) != 0) active.add(translationProvider.translateDtcStatusBit("Pending DTC", "Pending DTC"));
		if ((status & 0x08) != 0) active.add(translationProvider.translateDtcStatusBit("Confirmed DTC", "Confirmed DTC"));
		if ((status & 0x10) != 0) active.add(translationProvider.translateDtcStatusBit("Test Not Completed Since Last Clear", "Test Not Completed Since Last Clear"));
		if ((status & 0x20) != 0) active.add(translationProvider.translateDtcStatusBit("Test Failed Since Last Clear", "Test Failed Since Last Clear"));
		if ((status & 0x40) != 0) active.add(translationProvider.translateDtcStatusBit("Test Not Completed This Operation Cycle", "Test Not Completed This Operation Cycle"));
		if ((status & 0x80) != 0) active.add(translationProvider.translateDtcStatusBit("Warning Indicator Requested", "Warning Indicator Requested"));

		return active;
	}
}