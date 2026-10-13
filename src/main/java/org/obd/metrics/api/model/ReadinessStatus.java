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

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Decoded mode 01 PID 01 (since DTCs cleared) or PID 41 (this drive cycle). PID 41 carries no MIL state or DTC
 * count (its byte A is reserved): both are then false / 0.
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ReadinessStatus {
	private final boolean milOn;
	private final int emissionDtcCount;
	// Diesel: selects the meaning of the non-continuous monitors.
	private final boolean compressionIgnition;
	// Continuous monitors first, then the non-continuous ones in bit order; reserved bits are left out.
	private final List<ReadinessMonitor> monitors;
}
