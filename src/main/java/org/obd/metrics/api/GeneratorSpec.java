package org.obd.metrics.api;

import lombok.Builder;
import lombok.Getter;

@Builder
public class GeneratorSpec {
	protected static final double DEFAULT_GENERATOR_INCREMENT = 5.0;

	@Getter
	boolean enabled;

	Double increment;

	public Double getIncrement() {
		return increment == null ? DEFAULT_GENERATOR_INCREMENT : increment;
	}
}
