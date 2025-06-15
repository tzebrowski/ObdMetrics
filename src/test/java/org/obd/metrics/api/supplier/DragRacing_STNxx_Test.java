 /**
 * Copyright 2019-2025, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.api.supplier;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.CommandsSuplier;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.PidDefinitionOverride;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;


public class DragRacing_STNxx_Test {
	
	//7046, 7008, 7005, 7021, 7047, 7028, 
	@Test
	public void overrideTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(7046L)
				.pid(7008L)
				.pid(7005L)
				.pid(7005L)
				.pid(7021L)
				.pid(7047L)
				.pid(7028L)
				.pid(7007L)
				.pid(7036L)
				.build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.FALSE)
						.promoteAllGroupsEnabled(Boolean.FALSE)
						.build())
				
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(false)
						.enabled(Boolean.TRUE).build())
				.override(7047L,PidDefinitionOverride.builder().priority(0).build())
				.override(7036L,PidDefinitionOverride.builder().priority(0).build())
				.override(7021L,PidDefinitionOverride.builder().priority(0).build())
				.override(7028L,PidDefinitionOverride.builder().priority(4).build())
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DA18F1").mode("555").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();

		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1002 1000 1937 1924 18F0 1956 0300");
		
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA18F1, D:22 1018");
		Assertions.assertThat(collection.get(1).getPriority()).isEqualTo(4);
		
	}
}
