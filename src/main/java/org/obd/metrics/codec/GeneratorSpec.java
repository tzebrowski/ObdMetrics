package org.obd.metrics.codec;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public class GeneratorSpec {

	public static GeneratorSpec DEFAULT = GeneratorSpec.builder().enabled(Boolean.FALSE).build();

	protected static final double DEFAULT_GENERATOR_INCREMENT = 5.0;

	@Getter
	boolean enabled;

	@Getter
	@Default
	boolean smart = false;

	Double increment;

	public Double getIncrement() {
		return increment == null ? DEFAULT_GENERATOR_INCREMENT : increment;
	}
}
