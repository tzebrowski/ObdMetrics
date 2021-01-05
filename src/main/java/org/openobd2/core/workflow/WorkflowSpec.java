package org.openobd2.core.workflow;

import java.io.InputStream;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.connection.Connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class WorkflowSpec {

	@Getter
	private final Connection connection;

	@Getter
	private InputStream source;
	
	@Getter
	private String evaluationEngine;
	
	@Getter
	private CommandReplySubscriber subscriber;
}
