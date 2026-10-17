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
package org.obd.metrics.pid;

public enum PIDsGroup {
	LIVEDATA,
	ROUTINE,
	METADATA, 
	DTC_READ, 
	DTC_CLEAR, 
	// Per-code follow-up requests (UDS 19 06 extended data, ...) - templates with a {dtc} placeholder.
	DTC_FOLLOW_UP,
	// DID definitions for decoding DTC snapshots (19 04) and follow-up data, looked up before livedata: a DID's
	// size or scaling in a freeze frame can differ from its live (22) reply. Never queried as live data.
	DTC_SNAPSHOT,
	CAPABILITES,
	// OBD-II trouble code services (03 / 07 / 0A, clear 04), sent instead of DTC_READ / DTC_CLEAR to the modules
	// whose Init.Header says DtcProtocol.OBD.
	DTC_OBD_READ,
	DTC_OBD_CLEAR
}
