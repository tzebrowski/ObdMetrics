package org.obd.metrics.codec.batch;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchMessagePattern {
	@Getter
	private final List<BatchMessagePatternEntry> entries = new ArrayList<>();

	@Getter
	private int hit;

	void updateCacheHit() {
		hit++;
	}
}