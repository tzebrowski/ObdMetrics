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
package org.obd.metrics.transport.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.obd.metrics.api.CommandsSuplier;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.generator.GeneratorPolicy;
import org.obd.metrics.codec.generator.Strategy;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.mock.SmartMockAdapterConnection.OutStream;

import com.google.common.collect.Iterables;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SmartMockConnectionFactory {

	@Builder(builderMethodName = "smartBuilder", builderClassName = "SmartConnectionBuilder")
	public static AdapterConnection build(@NonNull final PidDefinitionRegistry registry,
			@NonNull final Adjustments optional,
			@NonNull final Query query, 
			@NonNull final Init init,
			@NonNull final Strategy strategy, int responseCount,
			@NonNull final String jsEngineName) {

		log.info("Building AdapterConnection for strategy={} , responseCount={} ", strategy, responseCount);

		final GeneratorPolicy policy = GeneratorPolicy.builder().enabled(true).strategy(strategy).build();
		final EcuResponseGenerator multiFrameGenerator = new EcuResponseGenerator(jsEngineName, registry);
		final CommandsSuplier commandsSuplier = new CommandsSuplier(registry, optional, query, init);
		final List<ObdCommand> commandList = commandsSuplier.get();

		log.info("Prepared {} commands", commandList.size());
		
		final Map<String, Iterator<String>> sharedRequestResponse = new ConcurrentHashMap<>(genericAnswers());
		final ExecutorService threadPool = ForkJoinPool.commonPool();
		
		final long totalStartTime = System.currentTimeMillis();
		
		
		final List<CompletableFuture<Void>> generationTasks = commandList.stream()
			.map(e -> CompletableFuture.runAsync(() -> {
				final String ecuQuery = e.getQuery();
				final String theadName = Thread.currentThread().getName();
				
				log.info("[{}] Generating ECU answers for: {}",theadName, ecuQuery);
				
				long queryStartTime = System.currentTimeMillis();
				final List<String> answers = multiFrameGenerator.generateAnswers(ecuQuery, responseCount, policy);
				final long queryExecutionTime = System.currentTimeMillis() - queryStartTime;
				
				log.info("[{}] Built {} ECU answers for query {} in {} ms",theadName, responseCount, ecuQuery, queryExecutionTime);

				sharedRequestResponse.put(ecuQuery, Iterables.cycle(answers).iterator());
				
			}, threadPool)) 
			.collect(Collectors.toList());

		CompletableFuture.allOf(generationTasks.toArray(new CompletableFuture[0]))
			.thenRun(() ->  {
				final long totalExecutionTime = System.currentTimeMillis() - totalStartTime;
				log.info("All background ECU answer generation completed in {} ms.", totalExecutionTime);
			});

		final MutableByteArrayInputStream input = new MutableByteArrayInputStream(0, false);
		return new SmartMockAdapterConnection(new OutStream(sharedRequestResponse, input, 0L, false), input, false);
	}

	@Builder(builderMethodName = "defaultBuilder", builderClassName = "DefaultConnectionBuilder")
	public static AdapterConnection build(
			@Singular("requestResponse") Map<String, List<String>> requestResponse, 
			final long writeTimeout, 
			final long readTimeout,
			final boolean simulateWriteError, 
			final boolean simulateReadError, 
			final boolean simulateErrorInReconnect) {

		final MutableByteArrayInputStream input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		return new SmartMockAdapterConnection(
				new OutStream(wrap(requestResponse), input, writeTimeout, simulateWriteError), input,
				simulateErrorInReconnect);
	}

	private static Map<String, Iterator<String>> wrap(Map<String, List<String>> parameters) {
		final Map<String, Iterator<String>> answers = new HashMap<>();
		answers.putAll(genericAnswers());
		parameters.forEach((k, v) -> {
			answers.put(k, Iterables.cycle(v).iterator());
		});
		return answers;
	}

	private static Map<String, Iterator<String>> genericAnswers() {
		final Map<String, Iterator<String>> requestResponse = new HashMap<>();
		requestResponse.put("ATZ", Iterables.cycle("connected?").iterator());
		requestResponse.put("ATL0", Iterables.cycle("atzelm327v1.5").iterator());
		requestResponse.put("ATH0", Iterables.cycle("ath0ok").iterator());
		requestResponse.put("ATE0", Iterables.cycle("ate0ok").iterator());
		requestResponse.put("ATSP0", Iterables.cycle("ok").iterator());
		requestResponse.put("ATSP6", Iterables.cycle("ok").iterator());
		requestResponse.put("ATSP7", Iterables.cycle("ok").iterator());
		requestResponse.put("ATAL", Iterables.cycle("ok").iterator());
		requestResponse.put("ATAT2", Iterables.cycle("ok").iterator());
		requestResponse.put("AT I", Iterables.cycle("elm327v1.5").iterator());
		requestResponse.put("AT @1", Iterables.cycle("obdiitors232interpreter").iterator());
		requestResponse.put("AT @2", Iterables.cycle("?").iterator());
		requestResponse.put("AT DP", Iterables.cycle("auto").iterator());
		requestResponse.put("AT DPN", Iterables.cycle("a0").iterator());
		requestResponse.put("AT RV", Iterables.cycle("11.8v").iterator());
		return requestResponse;
	}
}