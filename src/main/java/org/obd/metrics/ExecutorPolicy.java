package org.obd.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ExecutorPolicy {

	public static final ExecutorPolicy DEFAULT = ExecutorPolicy.builder().frequency(80).build();

	@Getter
	private final long frequency;
}
