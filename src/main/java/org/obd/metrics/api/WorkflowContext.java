package org.obd.metrics.api;

import java.util.Collections;
import java.util.Set;

import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.connection.Connection;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
public class WorkflowContext {

	@Getter
	@Default
	private final Set<Long> filter = Collections.emptySet();

	@Getter
	private final boolean batchEnabled;

	@Getter
	private final Connection connection;

	@Getter
	@Default
	private final GeneratorSpec generator = GeneratorSpec.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTiming = AdaptiveTimeoutPolicy.DEFAULT;
}
