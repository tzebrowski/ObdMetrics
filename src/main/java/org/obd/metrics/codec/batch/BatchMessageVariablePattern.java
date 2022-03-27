package org.obd.metrics.codec.batch;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchMessageVariablePattern {
	@Getter
	private final List<BatchMessageVariablePatternEntry> entries = new ArrayList<>();

	@Getter
	private int hit;

	void updateCacheHit() {
		hit++;
	}
}