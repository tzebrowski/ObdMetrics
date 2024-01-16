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
package org.obd.metrics.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class PIDsGroupReader <T> extends ReplyObserver<Reply<?>> {

	private final PIDsGroup group;

	@Getter
	protected T value;
	
	@Override
	public List<Class<?>> subscribeFor() {
		final Set<Class<?>> collect = Context.instance()
			.resolve(PidDefinitionRegistry.class)
			.get()
			.findBy(group)
			.stream()
			.map(p -> {
				try {
					return p.getCommandClass() == null ? null : Class.forName(p.getCommandClass());
				} catch (ClassNotFoundException e) {
					return null;
				}
			})
			.filter(p -> p != null)
			.collect(Collectors.toSet());
		
		collect.add(group.getDefaultCommandClass());
		return new ArrayList<>(collect);
	}
}
