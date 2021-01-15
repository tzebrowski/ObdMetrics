package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusListener;
import org.openobd2.core.connection.Connection;

import lombok.NonNull;

public interface Workflow {

	void start(Connection connection, Set<String> pids);

	default void start(Connection connection) {
		start(connection, Collections.emptySet());
	}

	void stop();

	public static Workflow mode1(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusListener statusListener) throws IOException {
		return new Mode1Workflow(equationEngine, subscriber, statusListener);
	}

	public static Workflow generic(@NonNull EcuSpecific ecuSpecific,@NonNull String equationEngine,
			@NonNull CommandReplySubscriber subscriber, StatusListener statusListener) throws IOException {
		return new GenericWorkflow(ecuSpecific,equationEngine, subscriber, statusListener);
	}
}
