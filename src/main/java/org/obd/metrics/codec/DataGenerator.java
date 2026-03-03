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
package org.obd.metrics.codec;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class DataGenerator implements Codec<Void, Number> {

    // Used ConcurrentHashMap to ensure thread safety during concurrent OBD decoding
    private final Map<PidDefinition, Double> generatorData = new ConcurrentHashMap<>();
    private final GeneratorPolicy generatorPolicy;
    private final Random random = new Random();

    @Override
    public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
        return generate(pid);
    }

    private Number generate(final PidDefinition pid) {
        // Fallback for missing bounds
        if (pid.getMin() == null || pid.getMax() == null) {
            return random.nextDouble() * 100.0; // Scaled fallback instead of 0.0-1.0
        }

        double min = pid.getMin().doubleValue();
        double max = pid.getMax().doubleValue();
        
        // Get the current value, defaulting to a random starting point within the valid range
        Double current = generatorData.computeIfAbsent(pid, k -> min + (random.nextDouble() * (max - min)));

        // Calculate the next step
        current = calculateNextRandomWalkValue(current, min, max);

        generatorData.put(pid, current);
        return current;
    }

    /**
     * Creates a realistic "Random Walk" fluctuation.
     * The value will wander up and down naturally within the min/max bounds.
     */
    private Double calculateNextRandomWalkValue(double current, double min, double max) {
        double range = max - min;
        
        // Define maximum allowed change per step (e.g., 5% of the total range)
        // You could also replace 0.05 with generatorPolicy.getIncrement() if it represents a percentage
        double maxStep = range * 0.05; 
        
        // Generate a random step between -maxStep and +maxStep
        double step = (random.nextDouble() * 2 * maxStep) - maxStep;
        
        double nextValue = current + step;

        // Clamp the value so it never exceeds the PID's hardware limits
        if (nextValue > max) {
            return max;
        } else if (nextValue < min) {
            return min;
        }

        return nextValue;
    }
}