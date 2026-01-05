 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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
package org.obd.metrics.api.model;

import java.util.Set;
import java.util.HashSet;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.Builder.Default;

@Builder
@ToString
public class SniffingPolicy {
	
	@Getter
	@Default
	private boolean debugEnabled = false;

	
	@Getter
	@Default
	private boolean enabled = false;
	
	@Builder
	@ToString
	public static class STNxxExtensions {
		@Getter
		@Default
		private boolean enabled = false;
		
		@Getter
		@Singular
		private Set<String> filters = new HashSet<String>();
	}
	
	@Getter
	@Default
	private STNxxExtensions stNxx = STNxxExtensions.builder().build();

}