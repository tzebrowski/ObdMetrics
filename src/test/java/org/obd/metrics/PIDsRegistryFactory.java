/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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
package org.obd.metrics;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry.PidDefinitionRegistryBuilder;
import org.obd.metrics.pid.Resource;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
class PIDsRegistryProxy implements PIDsRegistry {
	@Delegate
	final PidDefinitionRegistry registry;
}

public class PIDsRegistryFactory {
	
	static final Map<String, PIDsRegistry> cache = new HashedMap<>();

	public static PIDsRegistry get(final String... sources) {
		final String key = Stream.of(sources)
		        .map(Object::toString)
		        .collect(Collectors.joining(", "));

		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			PidDefinitionRegistryBuilder builder = PidDefinitionRegistry.builder();
			for (final String source : sources) {
				final InputStream inputStream = Thread.currentThread().getContextClassLoader()
				        .getResourceAsStream(source);
				builder = builder.source(Resource.builder().inputStream(inputStream).name(source).build());
			}
			
			final PIDsRegistryProxy proxy = new PIDsRegistryProxy(builder.build());
			cache.put(key, proxy);
			return proxy;
		}
	}
}