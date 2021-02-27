package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

public class WorkflowFactoryTest {

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
}
