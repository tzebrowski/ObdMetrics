package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

interface SimpleWorkflowFactory {

	static Workflow getMode01Workflow(final Lifecycle lifecycle) throws IOException {
		return getMode01Workflow(lifecycle, new DataCollector());
	}

	static Workflow getMode01Workflow() throws IOException {
		return getMode01Workflow(new LifecycleImpl(), new DataCollector());
	}

	static Workflow getMode01Workflow(final DataCollector dataCollector) throws IOException {
		return getMode01Workflow(new LifecycleImpl(), dataCollector);
	}

	static Workflow getMode22Workflow() throws IOException {
		return getMode22Workflow(new LifecycleImpl(), new DataCollector());
	}

	static Workflow getMode22Workflow(final DataCollector dataCollector) throws IOException {
		return getMode22Workflow(new LifecycleImpl(), dataCollector);
	}

	static Workflow getMode22Workflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return WorkflowFactory.generic().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(dataCollector)
		        .initialize();
	}

	static Workflow getMode01Workflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return WorkflowFactory.mode1().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(Mode1CommandGroup.INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("mode01.json")).build())
		        .observer(dataCollector)
		        .initialize();
	}

}
