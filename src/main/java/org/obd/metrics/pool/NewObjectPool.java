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
package org.obd.metrics.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class NewObjectPool<T> implements ObjectAllocator<T> {
	final Class<T> clazz;
	
	NewObjectPool(final Class<T> clazz, final int capacity) {
		this.clazz = clazz;
	}

	@Override
	public T allocate() {
		try {
			Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);
			
			return declaredConstructor.newInstance();
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			log.error("Failed to inititiate instance of class={}", clazz, e);
			return null;
		}
	}
}
