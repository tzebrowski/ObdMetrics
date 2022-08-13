package org.obd.metrics.api.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
public class ProducerPolicy {

	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .build();

	@Getter
	@Default
	private boolean priorityQueueEnabled = Boolean.TRUE;
}
