package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusListener;
import org.openobd2.core.connection.Connection;

import lombok.NonNull;

public interface Workflow {

	void start(Connection connection, Set<String> selectedPids);

	default void start(Connection connection) {
		start(connection, Collections.emptySet());
	}

	void stop();

	public static Workflow mode1(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusListener state) throws IOException {
		return new Mode1Workflow(equationEngine, subscriber, state);
	}

	public static Workflow mode22(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusListener state) throws IOException {
		return new Mode22Workflow(equationEngine, subscriber, state);
	}
}
