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
package org.obd.metrics.api.model;

import java.net.URL;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Builder
public class Pids {

	public static final Pids DEFAULT = Pids
	        .builder()
	        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
	        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
	        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
	@Getter
	@NonNull
	@Singular("resource")
	private List<URL> resources;
}
