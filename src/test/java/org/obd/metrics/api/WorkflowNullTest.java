package org.obd.metrics.api;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Pids;

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
