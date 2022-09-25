package org.obd.metrics.codec;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder 
public final class GeneratorPolicy {

	public static GeneratorPolicy DEFAULT = GeneratorPolicy.builder().enabled(false).build();

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
