package org.openobd2.core;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ExecutorPolicy {

	public static final ExecutorPolicy DEFAULT = ExecutorPolicy.builder().frequency(100).delayBeforeExecution(20).build();

	@Getter
	private final long frequency;

	@Getter
	private final long delayBeforeExecution;
}
