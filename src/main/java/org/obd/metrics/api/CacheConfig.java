package org.obd.metrics.api;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public final class CacheConfig {

	public static final CacheConfig DEFAULT = CacheConfig
	        .builder()
	        .build();
	
	@Getter
	@Default
	private boolean resultCacheEnabled = Boolean.TRUE;
	
	@Getter
	@Default
	private int resultCacheSize = 100000;
}
