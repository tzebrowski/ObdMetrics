package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

public class WorkflowNullTest {

	@Test
	public void genericNullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.generic().pidSpec(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.generic().pidSpec(
			        PidSpec.builder().initSequence(Mode1CommandGroup.INIT_NO_DELAY)
			                .pidFile(Urls.resourceToUrl("mode01.json")).build())
			        .initialize();
		});

	}

	@Test
	public void mode1NullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.mode1().pidSpec(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.mode1().pidSpec(
			        PidSpec.builder().initSequence(Mode1CommandGroup.INIT_NO_DELAY)
			                .pidFile(Urls.resourceToUrl("mode01.json")).build())
			        .initialize();
		});

	}

	@Test
	public void nullContextTest() throws IOException {
		final Workflow workflow = WorkflowFactory.generic()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(new DataCollector())
		        .initialize();

		Assertions.assertThrows(NullPointerException.class, () -> {
			workflow.start(null);
		});

	}

	@Test
	public void nullConnectionTest() throws IOException {
		final Workflow workflow = WorkflowFactory.generic()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(new DataCollector())
		        .initialize();

		Assertions.assertThrows(NullPointerException.class, () -> {
			workflow.start(WorkflowContext.builder().build());
		});

	}

}
