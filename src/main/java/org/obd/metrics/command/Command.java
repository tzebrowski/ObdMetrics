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
package org.obd.metrics.command;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	protected final String uid = UUID.randomUUID().toString();

	@Getter
	protected final String query;

	@Getter
	protected final String label;

	@Getter
	protected final byte[] data;

	@Getter
	protected final String service;
	
	@Getter
	protected final String serviceOverrides;
	
	protected Command(final String query, final String service, final String label) {
		this(query,service,label,"");
	}
	
	protected Command(final String query, final String service, final String label, final String serviceOverrides) {
		this.query = query;
		this.label = label;
		this.service = service;
		this.data = (query + "\r").getBytes();
		this.serviceOverrides = serviceOverrides;
	}

	@Override
	public String toString() {
		return "[query=" + query + "]";
	}
}
