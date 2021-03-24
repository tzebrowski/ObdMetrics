package org.obd.metrics.api;

import java.util.Collections;
import java.util.Set;

import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.connection.StreamConnection;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class WorkflowContext {

	@Getter
	@Default
	private final Set<Long> filter = Collections.emptySet();

	@Getter
	private final boolean batchEnabled;

	@Getter
	@NonNull
	private final StreamConnection connection;

	@Getter
	@Default
	private final GeneratorSpec generator = GeneratorSpec.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTiming = AdaptiveTimeoutPolicy.DEFAULT;

	@Getter
	@Default
	private final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;
}
