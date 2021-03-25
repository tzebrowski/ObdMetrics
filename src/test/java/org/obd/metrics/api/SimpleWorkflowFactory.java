package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.api.ConnectorTest.LifecycleImpl;
import org.obd.metrics.command.group.CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

public interface SimpleWorkflowFactory {

	static Workflow getMode01Workflow(final Lifecycle lifecycle) throws IOException {
		return getMode01Workflow(lifecycle, new DataCollector());
	}

	static Workflow getMode01Workflow(final DataCollector dataCollector) throws IOException {
		return getMode01Workflow(new LifecycleImpl(), dataCollector);
	}

	static Workflow getMode01Workflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return getWorkflow(Mode1CommandGroup.INIT_NO_DELAY, "mode01.json", lifecycle, dataCollector);
	}

	static Workflow getWorkflow(CommandGroup<?> commandGroup, String pidFile, Lifecycle lifecycle,
	        DataCollector dataCollector) throws IOException {
		final Workflow workflow = WorkflowFactory.mode1().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(commandGroup)
		                .pidFile(Urls.resourceToUrl(pidFile)).build())
		        .observer(dataCollector)
		        .initialize();
		return workflow;
	}
}
