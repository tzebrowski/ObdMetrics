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
package org.obd.metrics.codec.batch.decoder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Measures operations per unit of time
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1) // Warms up the JVM to trigger JIT compilation
@Measurement(iterations = 5, time = 1) // The actual measured runs
@Fork(0)
public class DecoderPerformanceTest {
	
	
	protected static final Adjustments ADJUSTEMENTS = Adjustments
	        .builder()
	        .cachePolicy(
	        		CachePolicy.builder()
	        		.storeResultCacheOnDisk(Boolean.FALSE)
	        		.resultCacheEnabled(Boolean.FALSE).build())
	        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
	                .builder()
	                .enabled(Boolean.FALSE)
	                .build())
	        .producerPolicy(ProducerPolicy.builder()
	                .priorityQueueEnabled(Boolean.FALSE)
	                .build())
	        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
	        .build();

	
	public static void main(String[] args) throws RunnerException {
	    Options opt = new OptionsBuilder()
	            .include(DecoderPerformanceTest.class.getSimpleName())
	            .measurementIterations(50)
	            .warmupIterations(5)
	            .forks(0)
	            .build();

	    new Runner(opt).run();
	}
	

    private BatchMessageDecoder decoder;
    private String query;
    private List<ObdCommand> commands;
    private ConnectorResponse connectorResponse;

    @Setup(Level.Trial)
    public void setup() {
    	
    	final String ecuAnswer = "7F227804E0:6210000000191:240000186B78182:27A15D182825A73:1937A15D181F634:B0180E000018675:2CF7186C00186D6:00186E00186F007:1002000018AD008:0018AE336018C79:3318AF000018C8A:03191008981911B:0898";
		
    	query = "STPX H:18DA10F1, D:22 1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
    	
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");

		commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
	
		decoder = BatchMessageDecoder.get(ADJUSTEMENTS.getBatchPolicy());
        
		connectorResponse = ConnectorResponseFactory.wrap(ecuAnswer.getBytes());
    }

    @Benchmark
    public void benchmarkDecode(Blackhole blackhole) {
        blackhole.consume(decoder.decode(query, commands, connectorResponse));
    }
}
