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
package org.obd.metrics.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Pids;
import org.obd.metrics.pid.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class Resources implements AutoCloseable {

	@Getter
	final List<Resource> resources;

	public static Resources convert(Pids pids) {
		final List<Resource> resources = pids.getResources().stream()
			.filter(p -> p != null)
			.map(p -> {
				try {
					final File file = new File(p.getFile());
					log.info("Loading resource file: {}. Files exists={}", file, file.exists());
					return Resource.builder().inputStream(p.openStream()).name(file.getName()).build();
	
				} catch (Throwable e) {
					log.warn("Failed to load resource file: {}", p.getFile(), e);
				}
				return null;
			}).filter(p -> p != null)
			.collect(Collectors.toList());
		return new Resources(resources);
	}

	@Override
	public void close() {

		resources.forEach(f -> {
			try {
				f.getInputStream().close();
			} catch (IOException e) {
			}
		});
	}
}