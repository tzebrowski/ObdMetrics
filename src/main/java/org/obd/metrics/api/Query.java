package org.obd.metrics.api;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public final class Query {

	@Getter
	@Singular("pid")
	private Set<Long> pids;
}
