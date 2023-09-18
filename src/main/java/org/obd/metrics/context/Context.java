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
package org.obd.metrics.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {

	private static final Context instance = new Context();

	private final Map<Class<? extends Service>, ? super Service> data = new HashMap<>();

	public <T extends Service> Bean<T> resolve(Class<T> clazz) {
		return Bean.of((T) data.get(clazz));
	}

	public <T extends Service> T forceResolve(Class<T> clazz) {
		return (T) data.get(clazz);
	}
	
	public <T extends Service> Bean<T> register(Class<T> clazz, T t) {
		data.remove(clazz);
		data.put(clazz, t);
		return Bean.of(t);
	}

	public Context reset() {
		data.clear();
		return this;
	}
	
	public static Context instance() {
		return instance;
	}

	public static void apply(Consumer<Context> action) {
		action.accept(instance);
	}
}
