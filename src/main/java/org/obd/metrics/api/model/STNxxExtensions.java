package org.obd.metrics.api.model;

import lombok.Getter;
import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class STNxxExtensions {

	/**
	 * Merge priority groups.
	 */
	@Getter
	@Default
	private boolean promoteSlowGroupsEnabled = Boolean.FALSE;
	
	/**
	 * Enables STN Family chip extensions.
	 */
	@Getter
	@Default
	private boolean enabled = Boolean.FALSE;
}
