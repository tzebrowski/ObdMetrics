package org.openobd2.core.workflow;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.command.group.CommandGroup;
import org.openobd2.core.connection.Connection;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface Workflow {

	void start(Connection connection,Set<String> selectedPids);

	void stop();

	@Builder(builderMethodName = "mode1")
	public static Workflow build(
			@NonNull InputStream source, 
			@NonNull String evaluationEngine,
			@NonNull CommandReplySubscriber subscriber, 
			State state,
			@Singular("initCommand") List<CommandGroup<?>> init) {
		return new Mode1Workflow(source, 
				evaluationEngine, 
				subscriber, 
				state == null ? new State.DummyState() : state,
				init);
	}
}
