package org.obd.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CommandLoopPolicy {

	public static final CommandLoopPolicy DEFAULT = CommandLoopPolicy.builder().frequency(10).build();

	@Getter
	private final long frequency;
}
