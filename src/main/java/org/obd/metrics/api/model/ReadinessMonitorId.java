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

/**
 * OBD-II readiness monitors (SAE J1979, mode 01 PIDs 01 and 41). The continuous monitors are the same for every
 * engine; the non-continuous ones depend on the ignition type, see {@link ReadinessStatus#isCompressionIgnition()}.
 */
public enum ReadinessMonitorId {
	// Continuous, byte B
	MISFIRE,
	FUEL_SYSTEM,
	COMPONENTS,

	// Non-continuous, spark ignition, bytes C/D bits 0-7
	CATALYST,
	HEATED_CATALYST,
	EVAPORATIVE_SYSTEM,
	SECONDARY_AIR,
	AC_REFRIGERANT,
	OXYGEN_SENSOR,
	OXYGEN_SENSOR_HEATER,

	// Non-continuous, compression ignition, bytes C/D bits 0, 1, 3, 5, 6 (2 and 4 reserved)
	NMHC_CATALYST,
	NOX_SCR,
	BOOST_PRESSURE,
	EXHAUST_GAS_SENSOR,
	PM_FILTER,

	// Bit 7 of bytes C/D for both ignition types
	EGR_VVT
}
