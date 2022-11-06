package org.obd.metrics.codec.batch;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchResponseMapping {

	@Getter
	private final List<BatchResponsePIDMapping> mappings = new ArrayList<>();

	@Getter
	private int hit;

	void updateCacheHit() {
		hit++;
	}
}