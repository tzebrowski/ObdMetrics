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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.translation.TranslationProvider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements CodecRegistry {

    private final Map<PidDefinition, Codec<?, ?>> registry = new ConcurrentHashMap<>();
    private final Codec<?, Number> fallbackCodec;
    private final PidDefinitionRegistry pidRegistry;
    private final FormulaEvaluatorConfig formulaEvaluatorConfig;
    private final TranslationProvider translationProvider;

    @Override
    public Codec<?, ?> findCodec(final PidDefinition pid) {
        return registry.computeIfAbsent(pid, this::loadCodec);
    }

    private Codec<?, ?> loadCodec(PidDefinition pid) {
        final String codecClass = pid.getCodecClass();

        if (codecClass != null && !codecClass.isEmpty()) {
            try {
                final Class<?> clazz = Class.forName(codecClass);

                try {
                    final Constructor<?> constructor = clazz.getDeclaredConstructor(FormulaEvaluatorConfig.class, PidDefinitionRegistry.class);
                    final Object newInstance = constructor.newInstance(formulaEvaluatorConfig, pidRegistry);
                    if (newInstance instanceof Codec) {
                        return (Codec<?, ?>) newInstance;
                    }
                } catch (Exception e) {
                    // Try constructor with TranslationProvider
                    try {
                        final Constructor<?> constructor = clazz.getDeclaredConstructor(TranslationProvider.class);
                        final Object newInstance = constructor.newInstance(translationProvider);
                        if (newInstance instanceof Codec) {
                            return (Codec<?, ?>) newInstance;
                        }
                    } catch (Exception e2) {
                        final Constructor<?> constructor = clazz.getConstructor();
                        final Object newInstance = constructor.newInstance();
                        if (newInstance instanceof Codec) {
                            return (Codec<?, ?>) newInstance;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to instantiate codec class: {}", codecClass, e);
            }
        }

        return fallbackCodec;
    }
}