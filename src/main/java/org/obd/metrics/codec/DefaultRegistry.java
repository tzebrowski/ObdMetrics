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

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements CodecRegistry {

	private final Map<PidDefinition, Codec<?, ?>> registry = new ConcurrentHashMap<>();
	private final Codec<?, Number> fallbackCodec;

	@Override
	public Codec<?, ?> findCodec(final PidDefinition pid) {
		return registry.computeIfAbsent(pid, codec -> {

			final String codecClass = pid.getCodecClass();
			
			if (codecClass != null && codecClass.length() > 0) {
				try {
					final Class<?> forName = Class.forName(codecClass);
					final Constructor<?> constructor = forName.getConstructor();
					final Object newInstance = constructor.newInstance();
					if (newInstance instanceof Codec<?, ?>) {
						return (Codec<?, ?>) newInstance;
					}
				} catch (Throwable e) {
				}
			}

			return fallbackCodec;
		});
	}
}
