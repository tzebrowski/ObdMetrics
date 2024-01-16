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

import java.util.List;

import org.obd.metrics.command.group.CommandGroup;
import org.obd.metrics.command.group.DefaultCommandGroup;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

@Builder
public class Init {
	
	@Builder
	@ToString
	public static class Header {
		@Getter
		@Default
		private String header = "";

		@Getter
		@Default
		private String mode = "";		
	}
	
	public static final Init DEFAULT = Init.builder()
	        .delayAfterInit(0)
	        .protocol(Protocol.AUTO)
	        .sequence(DefaultCommandGroup.INIT)
	        .build();

	public enum Protocol {
		AUTO(0), CAN_11(6), CAN_29(7);

		@Getter
		private int type;

		Protocol(int type) {
			this.type = type;
		}
	}
	
	@Getter
	@Default
	private long delayAfterReset = 0l;
	
	@Getter
	@Default
	private long delayAfterInit = 0l;

	@Getter
	@NonNull
	@Default
	private CommandGroup<?> sequence = DefaultCommandGroup.INIT;
	
	@Getter
	@Default
	private Protocol protocol = Protocol.AUTO;

	@Getter
	@Singular
	private List<Header> headers;
}