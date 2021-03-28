package org.obd.metrics.api;

import java.util.Collections;
import java.util.Set;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public class Query {
	@Getter
	@Default
	private final Set<Long> pids = Collections.emptySet();
}
