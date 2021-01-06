package org.openobd2.core.workflow;

import java.io.InputStream;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.connection.Connection;

import lombok.Builder;
import lombok.NonNull;

public interface Workflow {

	void start(Connection connection);

	void stop();

	@Builder(builderMethodName = "mode1")
	public static Workflow build(@NonNull InputStream source, @NonNull String evaluationEngine,
			@NonNull CommandReplySubscriber subscriber, State state) {

		return new Mode1Workflow(source, evaluationEngine, subscriber, state == null ? new State.DummyState() : state);
	}
}
