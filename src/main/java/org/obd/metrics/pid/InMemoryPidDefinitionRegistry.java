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
package org.obd.metrics.pid;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class InMemoryPidDefinitionRegistry implements PidDefinitionRegistry {

	private final Map<Long, PidDefinition> byId = new HashMap<>();
	private final MultiValuedMap<String, PidDefinition> byQuery = new ArrayListValuedHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final String IN_MEMORY_RESOURCE_ID = "memory";
	
	@Override
	public void register(@NonNull PidDefinition pidDefinition) {
		log.info("Register new pid: {}", pidDefinition);
		register(IN_MEMORY_RESOURCE_ID, PIDsGroup.LIVEDATA, pidDefinition);
	}

	@Override
	public void register(List<PidDefinition> pids) {
		pids.forEach(this::register);
	}

	@Override
	public PidDefinition findBy(@NonNull Long id) {
		return byId.get(id);
	}

	@Override
	public Collection<PidDefinition> findBy(PIDsGroup group) {
		return byQuery.values().stream().filter(p -> p.getGroup() == group)
				.sorted((a, b) -> a.getService().compareTo(b.getService())).collect(Collectors.toSet());
	}

	@Override
	public Collection<PidDefinition> findAllBy(PidDefinition pid) {
		if (pid == null) {
			return Collections.emptyList();
		}

		return byQuery.get(pid.getQuery());
	}

	@Override
	public Collection<PidDefinition> findAll() {
		return findBy(PIDsGroup.LIVEDATA);
	}

	void load(final Resource resource) {

		try {
			if (null == resource) {
				log.error("Was not able to load pids configuration");
			} else {
				long tt = System.currentTimeMillis();
				final PIDsGroupFile groupFile = objectMapper.readValue(resource.getInputStream(), PIDsGroupFile.class);

				registerPIDsGroup(groupFile.getDtcRead(), resource.getName(), PIDsGroup.DTC_READ);
				registerPIDsGroup(groupFile.getDtcClear(), resource.getName(), PIDsGroup.DTC_CLEAR);
				registerPIDsGroup(groupFile.getLivedata(), resource.getName(), PIDsGroup.LIVEDATA);
				registerPIDsGroup(groupFile.getMetadata(), resource.getName(), PIDsGroup.METADATA);
				registerPIDsGroup(groupFile.getCapabilities(), resource.getName(), PIDsGroup.CAPABILITES);

				tt = System.currentTimeMillis() - tt;
				log.info("Load {} PID definitions from stream. Operation took: {}ms", groupFile.getLivedata().size(),
						tt);
			}
		} catch (IOException e) {
			log.error("Failed to load definition file", e);
		}
	}

	private void registerPIDsGroup(final List<PidDefinition> data, final String resourceFile, final PIDsGroup group) {
		data.forEach(pid -> {
			register(resourceFile, group, pid);
		});
	}

	private void register(final String resourceFile, final PIDsGroup group, PidDefinition pid) {
		pid.setResourceFile(resourceFile);
		pid.setGroup(group);
		byQuery.put(pid.getQuery(), pid);
		byId.put(pid.getId(), pid);
	}
}
