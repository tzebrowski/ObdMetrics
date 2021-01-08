package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Set;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.connection.Connection;

import lombok.Builder;
import lombok.NonNull;

public interface Workflow {

	void start(Connection connection, Set<String> selectedPids);

	void stop();

	@Builder(builderMethodName = "mode1")
	public static Workflow m1(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			State state) throws IOException {
		
		return Mode1Workflow.builder().equationEngine(equationEngine).subscriber(subscriber).state(state).build();
	}

	@Builder(builderMethodName = "mode22")
	public static Workflow m22(@NonNull String equationEngine,
			@NonNull CommandReplySubscriber subscriber, State state) throws IOException {
		return Mode22Workflow.builder().equationEngine(equationEngine).subscriber(subscriber).state(state).build();
	}

}
