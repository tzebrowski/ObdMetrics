package org.obd.metrics.api.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public final class CachePolicy {

	public static final CachePolicy DEFAULT = CachePolicy
	        .builder()
	        .build();

	@Getter
	@Default
	private boolean resultCacheEnabled = Boolean.TRUE;

	@Getter
	@Default
	private int resultCacheSize = 100000;

	@Getter
	@Default
	private String resultCacheFilePath = "./result_cache.json";

	@Getter
	@Default
	private boolean storeResultCacheOnDisk = Boolean.TRUE;
}
