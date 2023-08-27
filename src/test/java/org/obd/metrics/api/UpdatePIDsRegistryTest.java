package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Urls;

public class UpdatePIDsRegistryTest {

	@Test
	public void updateTest() throws IOException, InterruptedException {

		// Getting the workflow - mode01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), new DataCollector(),"mode01.json");
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		Assertions.assertThat(pidRegistry.findBy(12l)).isNotNull();

		Assertions.assertThat(pidRegistry.findBy(7001L)).isNull();
		// Updating the registry with giulia_2.0_gme
		workflow.updatePidRegistry(Pids.builder().resource(Urls.resourceToUrl("giulia_2.0_gme.json")).build());
		pidRegistry = workflow.getPidRegistry();
		 
		Assertions.assertThat(pidRegistry.findBy(7001L)).isNotNull();
		Assertions.assertThat(pidRegistry.findBy(12l)).isNull();
	}
}
