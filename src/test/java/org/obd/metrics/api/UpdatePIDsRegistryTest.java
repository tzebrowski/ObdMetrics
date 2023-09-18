/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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
