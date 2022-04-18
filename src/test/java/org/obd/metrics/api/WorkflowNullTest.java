package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

public class WorkflowNullTest {
	
	@Test
	public void mode1NullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			Workflow.instance().pidSpec(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			Workflow.instance().pidSpec(
			        PidSpec.builder().initSequence(Mode1CommandGroup.INIT)
			                .pidFile(Urls.resourceToUrl("mode01.json")).build())
			        .initialize();
		});
	}
}
