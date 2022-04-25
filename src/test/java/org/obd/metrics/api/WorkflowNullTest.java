package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowNullTest {
	
	@Test
	public void mode1NullTest() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> {
			Workflow.instance().pids(null).initialize();
		});

		Assertions.assertThrows(NullPointerException.class, () -> {
			Workflow.instance()
			        .pids(Pids.DEFAULT)
			        .initialize();
		});
	}
}
