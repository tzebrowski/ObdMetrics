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
package org.obd.metrics.codec.formula;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.decoder.BatchDecoder;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Measures operations per unit of time
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1) // Warms up the JVM to trigger JIT compilation
@Measurement(iterations = 50, time = 1) // The actual measured runs
@Fork(0)
public class FormulaEvaluatorPerformanceTest {
	
	protected static final Adjustments ADJUSTEMENTS = Adjustments
	        .builder()
	        .cachePolicy(
	        		CachePolicy.builder()
	        		.storeResultCacheOnDisk(Boolean.FALSE)
	        		.resultCacheEnabled(Boolean.TRUE).build())
	        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
	                .builder()
	                .enabled(Boolean.FALSE)
	                .build())
	        .producerPolicy(ProducerPolicy.builder()
	                .priorityQueueEnabled(Boolean.FALSE)
	                .build())
	        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
	        .build();

	
	private BatchDecoder decoder;
    private String query;
    private List<ObdCommand> commands;
    private CodecRegistry codecRegistry;
    private List<String> ecuAnswers;
    
    public static void main(String[] args) throws RunnerException {
	    Options opt = new OptionsBuilder()
	            .include(FormulaEvaluatorPerformanceTest.class.getSimpleName())
	            .measurementIterations(5)
	            .warmupIterations(3)
	            .forks(0)
	            .build();

	    new Runner(opt).run();
	}
	

    
    @Setup(Level.Trial)
    public void setup() {
    	
    	query = "STPX H:18DA10F1, D:22 1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
    	
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");

		commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
	
		decoder = BatchDecoder.get(ADJUSTEMENTS);
        
		codecRegistry = CodecRegistry.of(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build(), ADJUSTEMENTS);
		ecuAnswers = EcuMockGenerator.generateAnswers(query, 10);
    }

	private int benchmark() {
		
		for (final String answer : ecuAnswers) {
			final ConnectorResponse connectorResponse = ConnectorResponseFactory.wrap(answer.getBytes());

			final Map<ObdCommand, ConnectorResponse> decode = decoder.decode(query, commands, connectorResponse);
			decode.forEach((command, cr) -> {
				codecRegistry.findCodec(command.getPid()).decode(command.getPid(), cr);
			});
		}
		return 0;
	}

    @Benchmark
    public void benchmarkDecode(Blackhole blackhole) {
        blackhole.consume(benchmark());
    }
}
