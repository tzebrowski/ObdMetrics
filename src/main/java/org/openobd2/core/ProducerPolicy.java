package org.openobd2.core;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy.builder().delayBeforeInsertingCommands(50)
			.emptyBufferSleepTime(200).build();

	@Getter
	private final long delayBeforeInsertingCommands;

	@Getter
	private final long emptyBufferSleepTime;
}
