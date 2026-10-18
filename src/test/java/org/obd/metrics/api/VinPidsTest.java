 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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
package org.obd.metrics.api;

import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;

public class VinPidsTest {

	private static PidDefinitionRegistry registry(final String... files) {
		final PidDefinitionRegistry.PidDefinitionRegistryBuilder builder = PidDefinitionRegistry.builder();
		for (final String file : files) {
			final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
			builder.source(Resource.builder().inputStream(source).name(file).build());
		}
		return builder.build();
	}

	// The VIN PIDs are marked in the resource files, not recognised by their request.
	@Test
	public void vinPidsAreTheOnesMarkedWithTheVinCategory() {
		final PidDefinitionRegistry registry = registry("mode01.json", "giulia_2.0_gme.json", "mode09.json");

		Assertions.assertThat(VinPids.isVin(registry.findBy(11000L))).isTrue(); // 09 02, metadata
		Assertions.assertThat(VinPids.isVin(registry.findBy(17001L))).isTrue(); // 22 F190
		Assertions.assertThat(VinPids.isVin(registry.findBy(17002L))).isFalse(); // 22 F18C, ECU serial number
		// mode09.json's 09 02 carries no category: the request alone does not make a VIN PID.
		Assertions.assertThat(VinPids.isVin(registry.findBy(2L))).isFalse();
	}
}
