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

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultRegistry implements CodecRegistry {

	private final Map<PidDefinition, Codec<?, ?>> registry = new ConcurrentHashMap<>();
	private final Codec<?, Number> fallbackCodec;
	private final PidDefinitionRegistry pidRegistry;
	private final FormulaEvaluatorConfig formulaEvaluatorConfig;
	private final TranslationProvider translationProvider;

	DefaultRegistry(Codec<?, Number> fallbackCodec, PidDefinitionRegistry pidRegistry,
			FormulaEvaluatorConfig formulaEvaluatorConfig, TranslationProvider translationProvider) {
		this.fallbackCodec = fallbackCodec;
		this.pidRegistry = pidRegistry;
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.translationProvider = translationProvider;

		preloadCodecs();
	}

	private void preloadCodecs() {
		log.info("Pre-loading codecs for all available PIDs to prevent lazy-loading latency...");
		long start = System.currentTimeMillis();

		int count = 0;
		if (pidRegistry != null) {
			for (PidDefinition pid : pidRegistry.findAll()) {
				findCodec(pid);
				count++;
			}
		}

		log.info("Successfully pre-loaded {} codecs in {}ms", count, (System.currentTimeMillis() - start));
	}

	@Override
	public Codec<?, ?> findCodec(final PidDefinition pid) {
		return registry.computeIfAbsent(pid, this::loadCodec);
	}

	private Codec<?, ?> loadCodec(PidDefinition pid) {
		final String codecClass = pid.getCodecClass();

		if (codecClass != null && !codecClass.isEmpty()) {
			try {
				final Class<?> clazz = Class.forName(codecClass);
				final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 2 && params[0] == FormulaEvaluatorConfig.class
							&& params[1] == PidDefinitionRegistry.class) {

						constructor.setAccessible(true);
						final Object instance = constructor.newInstance(formulaEvaluatorConfig, pidRegistry);
						if (instance instanceof Codec) {
							return (Codec<?, ?>) instance;
						}
					}
				}

				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 1 && params[0] == TranslationProvider.class) {

						constructor.setAccessible(true);
						final Object instance = constructor.newInstance(translationProvider);
						if (instance instanceof Codec) {
							return (Codec<?, ?>) instance;
						}
					}
				}

				for (Constructor<?> constructor : constructors) {
					if (constructor.getParameterTypes().length == 0) {

						constructor.setAccessible(true);
						final Object instance = constructor.newInstance();
						if (instance instanceof Codec) {
							return (Codec<?, ?>) instance;
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