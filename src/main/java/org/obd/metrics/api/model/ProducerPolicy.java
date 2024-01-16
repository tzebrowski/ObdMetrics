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
package org.obd.metrics.api.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@ToString
@Builder
public class ProducerPolicy {
	@SuppressWarnings("serial")
	public static final Map<Integer, Integer> DEFAULT_COMMAND_PRIORITY = new HashMap<Integer, Integer>() {
		{
			put(0, 0);
			put(1, 5);
			put(2, 15);
			put(3, 35);
			put(4, 50);
			put(5, 100);
			put(6, 200);
			put(7, 500);
			put(8, 1000);
			put(9, 5000);
			put(10, 10000);
		}
	};
	
	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .pidPriorities(DEFAULT_COMMAND_PRIORITY)
	        .priorityQueueEnabled(Boolean.TRUE)
	        .build();

	@Getter
	@Default
	private boolean priorityQueueEnabled = Boolean.TRUE;
	
	
	@Getter
	@Singular("pidPriority")
	private Map<Integer, Integer> pidPriorities;
	
	@Getter
	@Default
	private long conditionalSleepSliceSize = 10;


	@Getter
	@Default
	private Boolean conditionalSleepEnabled = Boolean.TRUE;

}
