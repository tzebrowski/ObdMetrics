package org.obd.metrics.api;

import java.util.Set;

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
}
