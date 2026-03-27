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
package org.obd.metrics.translation;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.SimpleLifecycle;

public class TranslationsTest {
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"pl=7002=Temp. powietrzaza intercoolerem",
			"en=7002=Air TempPost IC",
	}, delimiter = '=')
	public void languagesTests(String lang, Long id, String desc) throws IOException, InterruptedException {
	
		final Workflow workflow =  Workflow
				.instance()
				.translationProvider(TranslationProvider.instance(lang))
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build())
		        .lifecycle(new SimpleLifecycle())
		        .pids(Pids.builder().resource(Urls.resourceToUrl("giulia_2.0_gme.json")).build())
		        .observer(new DataCollector())
		        .initialize();
		
		final PidDefinition pid = workflow.getPidRegistry().findBy(id);
		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pid.getDescription().replace("\n", "")).isEqualTo(desc);
	}
}
