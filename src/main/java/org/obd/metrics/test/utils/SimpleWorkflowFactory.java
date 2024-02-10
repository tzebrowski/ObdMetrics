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
package org.obd.metrics.test.utils;

import java.io.IOException;

import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.Urls;

public interface SimpleWorkflowFactory {

	static Workflow getWorkflow(final Lifecycle lifecycle) throws IOException {
		return getWorkflow(lifecycle, new DataCollector());
	}

	static Workflow getWorkflow() throws IOException {
		return getWorkflow(new SimpleLifecycle(), new DataCollector());
	}

	static Workflow getWorkflow(final DataCollector dataCollector) throws IOException {
		return getWorkflow(new SimpleLifecycle(), dataCollector);
	}

	static Workflow getWorkflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return getWorkflow(lifecycle, dataCollector, "mode01.json", "alfa.json", "extra.json");
	}

	static <T extends ReplyObserver> Workflow getWorkflow(Lifecycle lifecycle, T dataCollector, String... pidFiles)
	        throws IOException {

		Pids.PidsBuilder pids = Pids
		        .builder();

		for (final String pidFile : pidFiles) {
			pids = pids.resource(Urls.resourceToUrl(pidFile));
		}
		return Workflow
				.instance()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build())
		        .lifecycle(lifecycle)
		        .pids(pids.build())
		        .observer(dataCollector)
		        .initialize();
	}
}
