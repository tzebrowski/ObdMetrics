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
package org.obd.metrics.pid;

import java.util.Collection;
import java.util.List;

import org.obd.metrics.context.Service;

import lombok.Builder;
import lombok.Singular;

public interface PidDefinitionRegistry extends Service {

	void register(PidDefinition def);

	void register(List<PidDefinition> pids);

	PidDefinition findBy(Long id);
	
	Collection<PidDefinition> findAllBy(PidDefinition pid);

	Collection<PidDefinition> findAll();
	
	Collection<PidDefinition> findBy(PIDsGroup group);
	
	@Builder
	static PidDefinitionRegistry build(@Singular("source") List<Resource> sources) {
		final InMemoryPidDefinitionRegistry instance = new InMemoryPidDefinitionRegistry();
		sources.forEach(instance::load);
		return instance;
	}
}