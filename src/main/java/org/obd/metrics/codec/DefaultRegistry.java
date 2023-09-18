/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.codec;

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
	public void register(final PidDefinition pid, final Codec<?> codec) {
		registry.put(pid, codec);
	}

	@Override
	public Codec<?> findCodec(final PidDefinition pid) {
		Codec<?> codec = registry.get(pid);

		if (null == codec) {
			// no dedicated codec
			codec = fallbackCodec;
		}
	
		return codec;
	}
}
