package org.obd.metrics.diagnostic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Rate {
	@Getter
	private final RateType type;

	@Getter
	private final double value;

	@Getter
	private final String key;
}