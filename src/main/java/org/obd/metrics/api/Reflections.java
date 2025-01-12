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
package org.obd.metrics.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class Reflections {
	
	private final Map<String, String> fallback;

	String getParameterizedType(Object o) {

		Class<?> clazz = o.getClass();
		log.debug("Getting parametrizedType for: {}", clazz.getName());

		while (clazz != null) {
			final Type genericSuperclass = clazz.getGenericSuperclass();
			if (genericSuperclass instanceof ParameterizedType) {
				String className = getClassName((ParameterizedType) genericSuperclass);
				if (null == className) {
					className = fallback.get(o.getClass().getName());
				}

				log.debug("Found parametrizedType: {} for: {}", className, clazz.getName());
				return className;
			}
			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private String getClassName(ParameterizedType superClass) {
		try {
			final String typeName = (superClass.getActualTypeArguments()[0]).getTypeName();
			final int indexOf = typeName.indexOf("<");
			return indexOf > 0 ? typeName.substring(0, indexOf) : typeName;

		} catch (Throwable e) {
			log.debug("Error occurred during fetching class name. ", e);
			return null;
		}
	}
}