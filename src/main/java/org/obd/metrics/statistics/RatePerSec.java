package org.obd.metrics.statistics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class RatePerSec {
	@Getter
	private final double value;

	@Getter
	private final String key;
}