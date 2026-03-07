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

import java.util.HashMap;
import java.util.Map;

final class DefaultDtcDictionary implements DtcDictionary {

	private static final Map<String, String> DTC_MAP = new HashMap<>();

	static {
		
	}

	private static final Map<String, String> FAILURE_TYPE_MAP = Map.ofEntries(
    );
	
	private static final Map<Character, String> POWERTRAIN_SUBSYSTEM_MAP = Map.of(
            '0', "Fuel and Air Metering and Auxiliary Emission Controls",
            '1', "Fuel and Air Metering",
            '2', "Fuel and Air Metering (Injector Circuit)",
            '3', "Ignition System or Misfire",
            '4', "Auxiliary Emission Controls",
            '5', "Vehicle Speed Control and Idle Control System",
            '6', "Computer and Output Circuits",
            '7', "Transmission",
            '8', "Transmission",
            '9', "Transmission"
    );
	

	private static final Map<Character, String> SYSTEM_MAP = Map.of(
            'P', "Powertrain",
            'C', "Chassis",
            'B', "Body",
            'U', "Network (UART)"
    );

    private static final Map<Character, String> CATEGORY_MAP = Map.of(
            '0', "Generic (SAE Standard)",
            '1', "Manufacturer Specific",
            '2', "Generic / Manufacturer Specific (depends on system)",
            '3', "Generic / Manufacturer Specific (depends on system)"
    );

    @Override
    public String getCategory(Character key, String defaultValue) {
		return CATEGORY_MAP.getOrDefault(key, defaultValue);
	}
    
    @Override
    public String getSystem(Character key, String defaultValue) {
		return SYSTEM_MAP.getOrDefault(key, defaultValue);
	}
	
    @Override
    public String getPowerTrain(Character key, String defaultValue) {
		return POWERTRAIN_SUBSYSTEM_MAP.getOrDefault(key, defaultValue);
	}
	
	
    @Override
    public String getFailureType(String key, String defaultValue) {
		return FAILURE_TYPE_MAP.getOrDefault(key, defaultValue);
	}
	
    @Override
    public String getDescription(String rawHex3Bytes) {
    	return DTC_MAP.getOrDefault(rawHex3Bytes, "Unknown DTC Description");
	}
}
