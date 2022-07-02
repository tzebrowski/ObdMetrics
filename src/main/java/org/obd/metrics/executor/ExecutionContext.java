package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obd.metrics.EventsPublishlisher;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Setter;

@Builder
public class ExecutionContext { 

	@Setter
	protected Connector connector;
	
	@Setter
	@Default
	protected Set<String> deviceCapabilities = new HashSet<>();

	@Setter
	@Default
	protected Map<String, String> deviceProperties = new HashMap<>();
	
	protected final CodecRegistry codecRegistry;
	protected final Lifecycle lifecycle;
	protected final EventsPublishlisher<Reply<?>> publisher;
	protected final PidDefinitionRegistry pids;
	protected final MetricValidator metricValidator = new MetricValidator();
}