package org.openobd2.core;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ExecutorPolicy {

	@Getter
	private long frequency;

	@Getter
	private long delayBeforeExecution;

} 
