package org.openobd2.core.workflow;

public interface Workflow {

	void start(WorkflowSpec spec) throws Exception;

	void stop();
	
	public static Workflow mode1() {
		return new Mode1Workflow();
	}
}
