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
package org.obd.metrics.api.model;

import java.util.LinkedList;

import org.obd.metrics.codec.GeneratorPolicy;
import org.obd.metrics.pid.PIDsGroup;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;


/**
 * It contains an additional settings used during workflow initialization.
 * 
 * @since 0.6.0
 * @author tomasz.zebrowski
 */
@ToString
@Builder
public final class Adjustments {
	
	public static final Adjustments DEFAULT = Adjustments.builder().build();

	@Getter
	@Default
	private boolean debugEnabled = false;
	
	
	@Getter
	@Default
	private STNxxExtensions stNxx = STNxxExtensions.builder().build();
	
	@Getter
	@Default
	private boolean collectRawConnectorResponseEnabled = Boolean.FALSE;
	
	/**
	 * Enables Vehicle Metadata Reading e.g: VIN, ECU Type, Hardware Versions.
	 */
	@Default
	private boolean vehicleMetadataReadingEnabled = Boolean.FALSE;
		
	/**
	 * Enables ECU Supported PIDs/Sensor reading.
	 */
	@Default
	private boolean vehicleCapabilitiesReadingEnabled = Boolean.FALSE;
	
	/**
	 * Enables ECU DTC Reading.
	 */
	@Default
	private boolean vehicleDtcReadingEnabled = Boolean.FALSE;
	
	/**
	 * Enables ECU DTC Cleaning procedure.
	 */
	@Default
	private boolean vehicleDtcCleaningEnabled = Boolean.FALSE;
	

	@Getter
	@Default
	private final GeneratorPolicy generatorPolicy = GeneratorPolicy.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTimeoutPolicy = AdaptiveTimeoutPolicy.DEFAULT;

	@Getter
	@Default
	private final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;

	@Getter
	@Default
	private final CachePolicy cachePolicy = CachePolicy.DEFAULT;
	
	@Getter
	@Default
	private final ErrorsPolicy errorsPolicy = ErrorsPolicy.DEFAULT;

	@Getter
	@Default
	private final BatchPolicy batchPolicy = BatchPolicy.DEFAULT;

	@Getter
	@Default
	private final UnitsConversionPolicy unitsConversionPolicy = UnitsConversionPolicy.DEFAULT;

	/***
	 * Returns all requested PIDs Groups.
	 * @see PIDsGroup
	 * @return all enabled PIDs groups.
	 */
	public LinkedList<PIDsGroup> getRequestedGroups() {
		
		final LinkedList<PIDsGroup> groups = new LinkedList<PIDsGroup>();

		if (vehicleMetadataReadingEnabled) {
			groups.add(PIDsGroup.METADATA);
		}
		
		if (vehicleDtcReadingEnabled) {
			groups.add(PIDsGroup.DTC_READ);
		}
		
		if (vehicleCapabilitiesReadingEnabled) {
			groups.add(PIDsGroup.CAPABILITES);
		}
		
		if (vehicleDtcCleaningEnabled) {
			groups.add(PIDsGroup.DTC_CLEAR);
		}
		
		return groups;
	}
}
