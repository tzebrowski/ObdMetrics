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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Lifecycle.Subscription;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Measures operations per unit of time
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1) // Warms up the JVM to trigger JIT compilation
@Measurement(iterations = 50, time = 1) // The actual measured runs
@Fork(0)
public class FormulaEvaluatorPerformanceTest {
	
	private final List<String> payloads = Arrays.asList(
			"7F22780550:6210005608191:24E2B9186B549B2:182710821828BA3:341937EBA3181F4:FFC9180ED652185:674B70186C8F706:186D592F186EA67:66186FF28610028:B63C18AD011D189:AE819618C7460EA:18AF237A18C840B:29191082E31911C:0E02",
			"7F22780550:621000C9E5191:24E612186BF1082:18270BC81828513:42193715ED181F4:3EC0180E8FD0185:679616186C91546:186DEFB3186EA37:F6186F65A410028:913218AD530E189:AE8D8F18C7A634A:18AFCC5B18C8C9B:6C19108CEC1911C:4E71",
			"7F22780550:6210005EFE191:245A83186B1FB62:1827EF201828803:3C19378EB9181F4:CC10180E06A0185:676DB0186CAE4B6:186D6ED6186E927:DE186FD0CD10028:AEE418ADFAC2189:AEC2F418C7A73EA:18AFECBA18C84EB:3C19104C031911C:E00D",
			"7F22780550:621000408C191:244E5D186BD3EB2:182768901828833:8B19374E3E181F4:F486180EAF91185:67D43D186CD1066:186DC483186E607:87186F6D0610028:3AFD18AD79F0189:AEB74318C73EA6A:18AFD7EA18C840B:9F1910CFEF1911C:D05D",
			"7F22780550:621000EE14191:241527186BBFA92:1827A17C1828613:401937CCB7181F4:11A3180EBCDB185:67C3DD186CF20F6:186D7FC6186E647:E6186F7A6A10028:B96C18ADCD5C189:AE3E1918C78BAAA:18AF45B118C89BB:BF191094A41911C:94B2",
			"7F22780550:621000E683191:249C39186BD1E82:1827739818285B3:851937ADAF181F4:4084180E66EF185:6719BC186C38CB6:186DE4FE186E587:5B186FA95010028:C67618ADEA22189:AEEE3818C7B2AEA:18AF103C18C8F0B:DC191064971911C:5116",
			"7F22780550:6210009DD4191:242290186B58632:1827BACF18285C3:AE1937D273181F4:E761180E2FB2185:677B30186CEB1F6:186D81E9186E3C7:2C186F097E10028:7F6318ADBBFB189:AEAB4018C76FCAA:18AF623018C8A7B:8B1910749A1911C:28B4",
			"7F22780550:62100006BD191:243B92186B92F92:18270CB71828893:0E1937E006181F4:2305180E5586185:67BC87186CBB486:186D364D186E797:33186FFB0C10028:1DA218AD3F60189:AE7CE618C742DBA:18AFE2FF18C812B:121910070D1911C:17C5",
			"7F22780550:62100087DF191:244B35186B2FDA2:1827C1E41828653:A81937F9E4181F4:C180180E475D185:678DD6186C0FF16:186DDBB2186EF77:84186F649510028:B4A518AD5E1C189:AEDDEB18C7A15BA:18AF709B18C80DB:02191067AE1911C:9643",
			"7F22780550:6210008E1C191:2416D3186B35542:1827064D1828A03:EC1937488C181F4:53E5180E08A8185:6792B2186CC8FB6:186DA773186EC07:63186F39C410028:A08518AD225F189:AE0FD118C72D1DA:18AF8D2318C89AB:C419105C591911C:123A",
			"7F22780550:6210005981191:24B7AC186B765C2:1827D0CB18288B3:BA193734A1181F4:F7D5180E63BB185:677C64186C49776:186D349F186E4A7:9B186F75D110028:BFAC18AD5302189:AE562918C7A284A:18AF587118C811B:05191027951911C:37B1",
			"7F22780550:621000D9E0191:24CA55186B43822:18278E741828FE3:7F1937C77A181F4:BA65180E510C185:671B8B186C08B96:186DBC59186E7F7:26186FF45010028:6B2A18AD623B189:AE376C18C74980A:18AFDFF918C8A5B:DB19103ECA1911C:F8D2",
			"7F22780550:621000F73B191:24D805186B6CD32:18278A5318288B3:F41937D9F1181F4:61AC180E8502185:67A12C186C8DAD6:186DD857186E057:F0186F7A9210028:652018AD1EE4189:AE3B1918C7A47BA:18AF550018C8DCB:6C1910AEA31911C:42C7");
	
	protected static final Adjustments ADJUSTEMENTS_CACHE_ENABLED = Adjustments
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


	protected static final Adjustments ADJUSTEMENTS_CACHE_DISABLED = Adjustments
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
	
	
	private BatchDecoder decoderCacheEnabled;
	private BatchDecoder decoderCacheDisabled;

	private CodecRegistry codecRegistry;
   
    
    public static void main(String[] args) throws RunnerException {
	    Options opt = new OptionsBuilder()
	            .include(FormulaEvaluatorPerformanceTest.class.getSimpleName())
	            .measurementIterations(15)
	            .warmupIterations(3)
	            .forks(0)
	            .build();

	    new Runner(opt).run();
	}
	
    
    @RequiredArgsConstructor
	static final class Input{
		final String query;
		final List<ObdCommand> commands;
		final ConnectorResponse bytes;
	}
	
	private final List<Input> testInput = new ArrayList<Input>();

    
    @Setup(Level.Trial)
    public void setup() {
    	
    	final String query = "STPX H:18DA10F1, D:22 1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
    	
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");

		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
	
		decoderCacheEnabled = BatchDecoder.get(ADJUSTEMENTS_CACHE_ENABLED);
		decoderCacheDisabled = BatchDecoder.get(ADJUSTEMENTS_CACHE_DISABLED);

		codecRegistry = CodecRegistry.of(
				FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build(), 
				ADJUSTEMENTS_CACHE_ENABLED, 
				new Subscription());
		
		for (int i=0; i<3; i++) {
			for (final String payload : payloads) {
				final ConnectorResponse bytes = ConnectorResponseFactory.wrap(payload.getBytes());
				testInput.add(new Input(query, commands, bytes));
			}
		}
		System.out.println("Number of entries:" + testInput.size());
    }

    @Benchmark
    public void benchmarkCacheEnabled(Blackhole blackhole) {
    	for (int i = 0; i < testInput.size(); i++) {
			final Input input = testInput.get(i);
			final Map<ObdCommand, ConnectorResponse> decode = decoderCacheEnabled.decode(input.query, 
					input.commands, input.bytes);
			
			for (final Entry<ObdCommand, ConnectorResponse> entry: decode.entrySet()) {
				Object value = codecRegistry.findCodec(
						entry.getKey().getPid()).decode(entry.getKey().getPid(), entry.getValue());
				blackhole.consume(value);
			};
		}
    }
    
    
    @Benchmark
    public void benchmarkCacheDisabled(Blackhole blackhole) {
    	for (int i = 0; i < testInput.size(); i++) {
			final Input input = testInput.get(i);
			final Map<ObdCommand, ConnectorResponse> decode = decoderCacheDisabled.decode(input.query, 
					input.commands, input.bytes);
			
			for (final Entry<ObdCommand, ConnectorResponse> entry: decode.entrySet()) {
				Object value = codecRegistry.findCodec(
						entry.getKey().getPid()).decode(entry.getKey().getPid(), entry.getValue());
				blackhole.consume(value);
			};
		}
    }
}
