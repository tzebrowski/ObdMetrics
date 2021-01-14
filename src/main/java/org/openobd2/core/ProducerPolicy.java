package org.openobd2.core;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	@Getter
	private long delayBeforeInsertingCommands;

	@Getter
	private long emptyBufferSleepTime;

} 
