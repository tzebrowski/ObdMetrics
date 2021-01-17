package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusObserver;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;

public interface Workflow {

	PidRegistry getRegistry();

	void start(Connection connection, Set<String> pids);

	default void start(Connection connection) {
		start(connection, Collections.emptySet());
	}
	
	void stop();
	
	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusObserver statusObserver, boolean batchEnabled) throws IOException {
		return new Mode1Workflow(equationEngine, subscriber, statusObserver,batchEnabled);
	}

	public static Workflow generic(@NonNull EcuSpecific ecuSpecific,@NonNull String equationEngine,
			@NonNull CommandReplySubscriber subscriber, StatusObserver statusObserver) throws IOException {
		return new GenericWorkflow(ecuSpecific,equationEngine, subscriber, statusObserver);
	}
}
