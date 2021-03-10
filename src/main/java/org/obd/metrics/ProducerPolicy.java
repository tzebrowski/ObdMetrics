package org.obd.metrics;

import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .commandFrequency(11)
	        .build();

	@Getter
	private final long commandFrequency;

	@Getter
	@Builder.Default
	private boolean adaptiveTimingEnabled = Boolean.TRUE;

	@Getter
	@Builder.Default
	private final long minimumTimeout = 20;

	@Getter
	@Builder.Default
	private final long commandFrequencyCheckInterval = TimeUnit.SECONDS.convert(10, TimeUnit.MILLISECONDS);

}
