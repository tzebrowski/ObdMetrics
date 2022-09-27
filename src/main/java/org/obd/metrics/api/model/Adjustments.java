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

	/**
	 * Enables STN Family chip extensions.
	 */
	@Getter
	@Default
	private boolean stnExtensionsEnabled = Boolean.FALSE;
	
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
	 * Enables batch queries so that multiple PIDSs are read within single request/response to the ECU.
	 */
	@Getter
	@Default
	private final boolean batchEnabled = Boolean.FALSE;

	/**
	 * Add number of lines expected to return by Adapter which speedups the communication between Lib->Adapter.
	 */
	@Getter
	@Default
	private final boolean responseLengthEnabled = Boolean.TRUE;
	

	@Getter
	@Default
	private final GeneratorPolicy generator = GeneratorPolicy.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTiming = AdaptiveTimeoutPolicy.DEFAULT;

	@Getter
	@Default
	private final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;

	@Getter
	@Default
	private final CachePolicy cacheConfig = CachePolicy.DEFAULT;
	
	/***
	 * Returns all requested Pid Groups.
	 * @see PIDsGroup
	 * @return all enabled Pid groups.
	 */
	public LinkedList<PIDsGroup> getRequestedGroups() {
		
		final LinkedList<PIDsGroup> groups = new LinkedList<PIDsGroup>();

		if (vehicleMetadataReadingEnabled) {
			groups.add(PIDsGroup.METADATA);
		}
		
		
		if (vehicleDtcReadingEnabled) {
			groups.add(PIDsGroup.DTC);
		}
		
		if (vehicleCapabilitiesReadingEnabled) {
			groups.add(PIDsGroup.CAPABILITES);
		}

		return groups;
	}
}
