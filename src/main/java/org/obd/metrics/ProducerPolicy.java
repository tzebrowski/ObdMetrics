package org.obd.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .timeoutBeforeInsertingCommand(100)
	        .build();

	@Getter
	private final long timeoutBeforeInsertingCommand;

}
