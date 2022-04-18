package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.api.PidSpec.PidSpecBuilder;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

public interface SimpleWorkflowFactory {

	static Workflow getMode01Workflow(final Lifecycle lifecycle) throws IOException {
		return getMode01Workflow(lifecycle, new DataCollector());
	}

	static Workflow getMode01WorkflowExtended(final DataCollector dataCollector) throws IOException {
		return getMode01Workflow(new SimpleLifecycle(), dataCollector, "mode01.json", "extra.json");
	}

	static Workflow getMode01Workflow() throws IOException {
		return getMode01Workflow(new SimpleLifecycle(), new DataCollector());
	}

	static Workflow getMode01Workflow(final DataCollector dataCollector) throws IOException {
		return getMode01Workflow(new SimpleLifecycle(), dataCollector);
	}

	static Workflow getMode22Workflow() throws IOException {
		return getMode22Workflow(new SimpleLifecycle(), new DataCollector());
	}

	static Workflow getMode22Workflow(final DataCollector dataCollector) throws IOException {
		return getMode22Workflow(new SimpleLifecycle(), dataCollector);
	}

	static Workflow getMode22Workflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return Workflow.instance().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(dataCollector)
		        .initialize();
	}

	static Workflow getMode01Workflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return getMode01Workflow(lifecycle, dataCollector, "mode01.json");
	}

	static Workflow getMode01Workflow(Lifecycle lifecycle, DataCollector dataCollector, String... pidFiles)
	        throws IOException {

		PidSpecBuilder pidSpecBuilder = PidSpec
		        .builder()
		        .initSequence(Mode1CommandGroup.INIT);

		for (final String pidFile : pidFiles) {
			pidSpecBuilder = pidSpecBuilder.pidFile(Urls.resourceToUrl(pidFile));
		}
		return Workflow.instance().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(pidSpecBuilder.build())
		        .observer(dataCollector)
		        .initialize();
	}
}
