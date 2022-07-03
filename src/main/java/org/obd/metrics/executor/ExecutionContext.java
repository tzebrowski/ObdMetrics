package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Reply;
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
	
	protected final EventsPublishlisher<Reply<?>> publisher;
}