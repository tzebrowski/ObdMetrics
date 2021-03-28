package org.obd.metrics.api;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public final class Query {

	@Getter
	@Singular("pid")
	private List<Long> pids;
}
