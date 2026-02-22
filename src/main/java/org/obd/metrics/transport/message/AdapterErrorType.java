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
package org.obd.metrics.transport.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AdapterErrorType {
	CANERROR("CANERROR".getBytes()), 
	BUSBUSY("BUSBUSY".getBytes()), 
	STOPPED("STOPPED".getBytes()), 
	ERROR("ERROR".getBytes()), 
	NO_DATA("NODATA".getBytes()), 
	NONE("NONE".getBytes()), 
	BUSINIT("BUSINIT".getBytes()), 
	UNABLETOCONNECT("UNABLETOCONNECT".getBytes()),
	LVRESET("LVRESET".getBytes()), 
	TIMEOUT("TIMEOUT".getBytes()),
	FCRXTIMEOUT("FCRXTIMEOUT".getBytes()),
	UNKNOWN("UNKNOWN".getBytes());
		
	public static AdapterErrorType map(String message) {
		if (null == message || message.length() == 0) {
			return AdapterErrorType.UNKNOWN;
		}
		
		for (final AdapterErrorType val: values()) {
			if (message.equals(val.name())) {
				return val;
			}
		}
		return AdapterErrorType.UNKNOWN;
	}
	
	@Getter
	private final byte []bytes;
}