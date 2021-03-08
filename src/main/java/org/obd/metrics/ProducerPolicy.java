package org.obd.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .beforeFeelingQueue(20)
	        .build();

	@Getter
	private final long beforeFeelingQueue;

}
