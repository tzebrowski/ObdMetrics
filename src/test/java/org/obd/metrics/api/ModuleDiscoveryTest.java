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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.ModuleDiscoveryStatus;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;
import org.obd.metrics.translation.TranslationProvider;

import lombok.NonNull;

public class ModuleDiscoveryTest {

	@SuppressWarnings("unchecked")
	private static <T extends ReplyObserver<?>> Workflow getWorkflow(Lifecycle lifecycle, T dataCollector) {
		return Workflow.instance()
				.translationProvider(TranslationProvider.instance("en"))
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build())
				.lifecycle(lifecycle)
				.pids(Pids.builder().resource(Urls.resourceToUrl("giulia_2.0_gme.json")).build())
				.observer((@NonNull ReplyObserver<Reply<?>>) dataCollector)
				.initialize();
	}

	@Test
	public void discoverModuleReportsFoundWhenModuleAnswers() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector);

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("3E00", "7E00")
				.build();

		workflow.start(connection, query, createDefaultInit(), createAdjustments());
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		WorkflowExecutionStatus status = workflow.discoverModule("DA10F1");
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.DISCOVERY_QUEUED);

		WorkflowFinalizer.finalizeAfter(workflow, 800);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		Assertions.assertThat(lifecycle.getDiscoveredModuleHeader()).isEqualTo("DA10F1");
		Assertions.assertThat(lifecycle.getModuleDiscoveryStatus()).isEqualTo(ModuleDiscoveryStatus.FOUND);

		Assertions.assertThat(connection.recordedQueries().toString())
				.contains("ATSHDA10F1")
				.contains("3E00");
	}

	@Test
	public void discoverModuleReportsNotFoundWhenNoModuleAnswers() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector);

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("3E00", "NODATA")
				.build();

		workflow.start(connection, query, createDefaultInit(), createAdjustments());
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		workflow.discoverModule("DAFFF1");

		WorkflowFinalizer.finalizeAfter(workflow, 800);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		Assertions.assertThat(lifecycle.getDiscoveredModuleHeader()).isEqualTo("DAFFF1");
		Assertions.assertThat(lifecycle.getModuleDiscoveryStatus()).isEqualTo(ModuleDiscoveryStatus.NOT_FOUND);
	}

	private Init createDefaultInit() {
		return Init.builder()
				.delayAfterInit(0)
				.header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.protocol(Protocol.CAN_29)
				.sequence(DefaultCommandGroup.INIT)
				.build();
	}

	private Adjustments createAdjustments() {
		return Adjustments.builder()
				.debugEnabled(false)
				.vehicleDtcReadingEnabled(false)
				.vehicleMetadataReadingEnabled(false)
				.vehicleCapabilitiesReadingEnabled(false)
				.cachePolicy(CachePolicy.builder()
						.storeResultCacheOnDisk(false)
						.resultCacheEnabled(false).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder()
						.enabled(false)
						.commandFrequency(6)
						.build())
				.producerPolicy(ProducerPolicy.builder()
						.priorityQueueEnabled(true)
						.build())
				.batchPolicy(BatchPolicy.builder().enabled(true).build())
				.build();
	}
}
