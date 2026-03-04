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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormulaEvaluatorCacheTest {
	
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

    
    @Test
    public void test() {
    	
    	
    	String query = "STPX H:18DA10F1, D:22 1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
    	
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");

		 List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
	
		BatchDecoder decoder = BatchDecoder.get(ADJUSTEMENTS);
		CodecRegistry codecRegistry = CodecRegistry.of(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build(), ADJUSTEMENTS);
		
		final List<String> ecuAnswers = EcuMockGenerator.generateAnswers(query, 100);
		
		for (final String answer : ecuAnswers) {
			final ConnectorResponse connectorResponse = ConnectorResponseFactory.wrap(answer.getBytes());
			
			final Map<ObdCommand, ConnectorResponse> decode = decoder.decode(query, commands, connectorResponse);
	        decode.forEach( (command,cr) ->{
	        	codecRegistry.findCodec(command.getPid()).decode(command.getPid(), cr);
	        }); 
		}
		
		//cache
		for (int i=0; i<2; i++) {
			log.error("----------------------------------------------------------------------------------");
			
			for (final String answer : ecuAnswers) {
				final ConnectorResponse connectorResponse = ConnectorResponseFactory.wrap(answer.getBytes());
				
				final Map<ObdCommand, ConnectorResponse> decode = decoder.decode(query, commands, connectorResponse);
		        decode.forEach( (command,cr) ->{
		        	Object value = codecRegistry.findCodec(command.getPid()).decode(command.getPid(), cr);
	//	        	log.error("{} = {}",command.getPid().getPid(),value);
		        }); 
			}
		}

		
    }
}
