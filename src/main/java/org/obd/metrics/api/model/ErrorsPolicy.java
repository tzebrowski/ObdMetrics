package org.obd.metrics.api.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
public final class ErrorsPolicy {
	public static final ErrorsPolicy DEFAULT = ErrorsPolicy.builder().build();
	
	@Default
	private boolean reconnectEnabled = false;

	@Default
	private int numberOfRetries = 3;
}