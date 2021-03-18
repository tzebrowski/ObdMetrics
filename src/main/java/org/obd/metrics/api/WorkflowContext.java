package org.obd.metrics.api;

import java.util.Set;

import org.obd.metrics.AdaptiveTimeoutPolicy;
import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.connection.Connection;

import lombok.Builder;
import lombok.Getter;

@Builder
public class WorkflowContext {

	@Getter
	Set<Long> filter;

	@Getter
	boolean batchEnabled;

	@Getter
	Connection connection;

	@Getter
	GeneratorSpec generator;

	AdaptiveTimeoutPolicy adaptiveTiming;

	public AdaptiveTimeoutPolicy getAdaptiveTiming() {
		return adaptiveTiming == null ? AdaptiveTimeoutPolicy.DEFAULT : adaptiveTiming;
	}

}
