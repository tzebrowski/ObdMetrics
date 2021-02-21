package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.group.Mode1CommandGroup;

public class WorkflowFactoryTest {

	@Test
	public void genericNullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.generic().ecuSpecific(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.generic().ecuSpecific(
			        EcuSpecific.builder().initSequence(Mode1CommandGroup.INIT_NO_DELAY).pidFile("mode01.json").build())
			        .initialize();
		});
		
	

	}
	
	@Test
	public void mode1NullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.mode1().ecuSpecific(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			WorkflowFactory.mode1().ecuSpecific(
			        EcuSpecific.builder().initSequence(Mode1CommandGroup.INIT_NO_DELAY).pidFile("mode01.json").build())
			        .initialize();
		});

	}
	

}
