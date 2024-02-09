/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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
package org.obd.metrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;
import org.obd.metrics.pid.ValueType;

public class PidDefinitionRegistryTest {

	@Test
	public void registerCollectionOfPids() throws IOException {
		PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition pidDefinition = new PidDefinition(10001l, 2, "A+B", "01", "CC", "C", "dummy pid", 0, 100,
				ValueType.DOUBLE);

		pidRegistry.register(java.util.Arrays.asList(pidDefinition));

		PidDefinition findBy = pidRegistry.findBy("CC");
		Assertions.assertThat(findBy).isNotNull();
		Assertions.assertThat(findBy.getId()).isEqualTo(10001l);
		Assertions.assertThat(findBy.getFormula()).isEqualTo("A+B");

	}

	@Test
	public void registerNullPid() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder()
					.source(Resource.builder().inputStream(source).name("mode01.json").build()).build();
			org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
				pidRegistry.register((PidDefinition) null);
			});
		}
	}

	@Test
	public void registerPid() throws IOException {
		PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition def = new PidDefinition(1000l, 2, "A+B", "01", "FF", "C", "dummy pid", 0, 100, ValueType.DOUBLE);

		pidRegistry.register(def);

		PidDefinition findBy = pidRegistry.findBy("FF");
		Assertions.assertThat(findBy).isNotNull();
		Assertions.assertThat(findBy.getId()).isEqualTo(1000l);
		Assertions.assertThat(findBy.getFormula()).isEqualTo("A+B");

	}

	@Test
	public void findByModeAndPid() throws IOException {
		PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition findBy = pidRegistry.findBy("0C");
		Assertions.assertThat(findBy).isNotNull();
	}

	@Test
	public void findByNull() {
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(null).build();
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
			pidRegistry.findBy((Long) null);
		});
	}

	@Test
	public void findAllBy() {
		PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		PidDefinition findBy = pidRegistry.findBy("15");

		final Collection<PidDefinition> o2 = pidRegistry.findAllBy(findBy);

		Assertions.assertThat(o2).hasSize(2);
		Iterator<PidDefinition> iterator = o2.iterator();

		PidDefinition n1 = iterator.next();
		PidDefinition n2 = iterator.next();

		Assertions.assertThat(n1.getId()).isEqualTo(22l);
		Assertions.assertThat(n2.getId()).isEqualTo(23l);
	}
	
	
	@ParameterizedTest
	@CsvSource(
			value = { 
				"7035=false",
				"7001=true",
			},
			delimiter = '=')
	public void histogramAvgValueTest(long id, boolean avgEnabled) throws IOException {
		final PIDsRegistry pidRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final PidDefinition findBy = pidRegistry.findBy(id);
		Assertions.assertThat(findBy).isNotNull();
		Assertions.assertThat(findBy.getHistorgam().isAvgEnabled()).isEqualTo(avgEnabled);
	}
	
	
	@ParameterizedTest
	@CsvSource(
			value = { 
				"7044=gearbox=giulia_2.0_gme.json",
				"7032=service=giulia_2.0_gme.json",
				"50=4wheel=jeep_drive_control_module.json",
				"6003=ecu=alfa.json",
				"22=ecu=mode01.json",
			},
			delimiter = '=')
	public void moduleTest(long id, String module, String resource) throws IOException {
		final PIDsRegistry pidRegistry = PIDsRegistryFactory.get(resource);

		final PidDefinition findBy = pidRegistry.findBy(id);
		Assertions.assertThat(findBy).isNotNull();
		Assertions.assertThat(findBy.getModule()).isEqualTo(module);
	}
}
