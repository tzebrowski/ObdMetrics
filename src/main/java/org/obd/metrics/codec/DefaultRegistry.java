 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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
import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements CodecRegistry {

	private final Map<PidDefinition, Codec<?>> registry = new HashMap<>();
	private final Codec<Number> fallbackCodec;

	@Override
	public Codec<?> findCodec(final PidDefinition pid) {
		Codec<?> codec = registry.get(pid);
		
		if (null == codec) {
			codec = getOrCreate(pid);
			
			if (null == codec) {
				// no dedicated codec
				codec = fallbackCodec;
			}
		}
	
		return codec;
	}

	private Codec<?> getOrCreate(final PidDefinition pid) {
		Codec<?> codec = null;
		final String codecClass = pid.getCodecClass();

		
		if (codecClass != null && codecClass.length() > 0) {
			try {
				final Class<?> forName = Class.forName(codecClass);
				final Constructor<?> constructor = forName.getConstructor();
				final Object newInstance = constructor.newInstance();
				if (newInstance instanceof Codec<?>) {
					codec = (Codec<?>) newInstance;
			 		//register the codec for second use
					registry.put(pid, codec);
				}
			} catch (Throwable e) {}
		}
		return codec;
	}
}
