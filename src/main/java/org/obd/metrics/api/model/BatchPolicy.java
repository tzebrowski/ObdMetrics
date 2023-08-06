package org.obd.metrics.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder 
public class BatchPolicy {
	public static final BatchPolicy DEFAULT = BatchPolicy.builder().enabled(false).build();

	/**
	 * Enables batch queries so that multiple PIDSs are read within single request/response to the ECU.
	 */
	@Getter
	@Default
	private final boolean enabled = Boolean.FALSE;
	
	@Getter
	private final Integer mode22BatchSize;
}