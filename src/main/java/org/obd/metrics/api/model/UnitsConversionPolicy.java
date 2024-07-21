package org.obd.metrics.api.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Builder
@ToString
public final class UnitsConversionPolicy {
	public static final UnitsConversionPolicy DEFAULT = UnitsConversionPolicy
			.builder()
			.param("unit_tank_size", 58)
			.build();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Getter
	@Singular
	private Map<String, Number> params = new HashMap();
}