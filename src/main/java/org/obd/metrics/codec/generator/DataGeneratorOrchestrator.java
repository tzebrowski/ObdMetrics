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
package org.obd.metrics.codec.generator;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class DataGeneratorOrchestrator implements Codec<Void, Number> {

    private final Map<PidDefinition, Double> generatorData = new ConcurrentHashMap<>();
    private final GeneratorPolicy generatorPolicy;
    private final Random random = new Random();

    @Override
    public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
        return generate(pid);
    }

    private Number generate(final PidDefinition pid) {
        if (pid.getMin() == null || pid.getMax() == null) {
            return random.nextDouble() * 100.0; 
        }

        final double min = pid.getMin().doubleValue();
        
        final Double current = generatorData.getOrDefault(pid, min);

        final Double nextValue = generatorPolicy.getStrategy().getGeneratorStrategy().calculateNext(pid, current);
        generatorData.put(pid, nextValue);
        return nextValue;
    }
}