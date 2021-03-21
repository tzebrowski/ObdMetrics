package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public class AdaptiveTimeoutPolicy {

	public static final AdaptiveTimeoutPolicy DEFAULT = AdaptiveTimeoutPolicy
	        .builder()
	        .build();

	@Getter
	@Default
	private final long commandFrequency = 11;

	@Getter
	@Default
	private boolean enabled = Boolean.TRUE;

	@Getter
	@Default
	private final long minimumTimeout = 60;

	@Getter
	@Default
	private final long checkInterval = TimeUnit.SECONDS.toMillis(10);

}
