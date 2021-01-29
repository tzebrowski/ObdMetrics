package org.obd.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy.builder().delayBeforeInsertingCommands(20)
			.emptyBufferSleepTime(200).build();

	@Getter
	private final long delayBeforeInsertingCommands;

	@Getter
	private final long emptyBufferSleepTime;
}
