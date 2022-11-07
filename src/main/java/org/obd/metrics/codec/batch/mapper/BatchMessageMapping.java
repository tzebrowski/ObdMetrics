package org.obd.metrics.codec.batch.mapper;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchMessageMapping {

	@Getter
	private final List<BatchCommandMapping> mappings = new ArrayList<>();

	@Getter
	private int hit;

	void updateCacheHit() {
		hit++;
	}
}