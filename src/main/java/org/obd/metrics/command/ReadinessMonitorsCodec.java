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
package org.obd.metrics.command;

import static org.obd.metrics.api.model.ReadinessMonitorId.*;

import java.util.ArrayList;
import java.util.List;

import org.obd.metrics.api.model.ReadinessMonitor;
import org.obd.metrics.api.model.ReadinessMonitorId;
import org.obd.metrics.api.model.ReadinessStatus;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Decodes mode 01 PID 01 (monitor status since DTCs cleared) and PID 41 (monitor status this drive cycle), SAE
 * J1979. Byte A: MIL (bit 7) and emission DTC count (bits 0-6), PID 01 only. Byte B: continuous monitors supported
 * (bits 0-2) and incomplete (bits 4-6), ignition type (bit 3). Bytes C / D: non-continuous monitors supported /
 * incomplete, bit meaning by ignition type.
 */
@Slf4j
public final class ReadinessMonitorsCodec implements Codec<Void, ReadinessStatus> {

	private static final String DRIVE_CYCLE_PID = "41";

	private static final ReadinessMonitorId[] CONTINUOUS = { MISFIRE, FUEL_SYSTEM, COMPONENTS };
	private static final ReadinessMonitorId[] SPARK = { CATALYST, HEATED_CATALYST, EVAPORATIVE_SYSTEM,
			SECONDARY_AIR, AC_REFRIGERANT, OXYGEN_SENSOR, OXYGEN_SENSOR_HEATER, EGR_VVT };
	// null = reserved bit
	private static final ReadinessMonitorId[] COMPRESSION = { NMHC_CATALYST, NOX_SCR, null, BOOST_PRESSURE, null,
			EXHAUST_GAS_SENSOR, PM_FILTER, EGR_VVT };

	@Override
	public ReadinessStatus decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		final String raw = connectorResponse.getRawValue(pid);
		// A functional request can be answered by several ECUs: the first 4 data bytes are the first reply.
		if (raw == null || raw.length() < 8) {
			log.warn("PID[{}]: no readiness data in '{}'", pid.getPid(), connectorResponse.getMessage());
			return null;
		}
		try {
			final int a = Integer.parseInt(raw.substring(0, 2), 16);
			final int b = Integer.parseInt(raw.substring(2, 4), 16);
			final int c = Integer.parseInt(raw.substring(4, 6), 16);
			final int d = Integer.parseInt(raw.substring(6, 8), 16);
			return decode(DRIVE_CYCLE_PID.equalsIgnoreCase(pid.getPid()), a, b, c, d);
		} catch (NumberFormatException e) {
			log.warn("PID[{}]: invalid readiness data '{}'", pid.getPid(), raw);
			return null;
		}
	}

	static ReadinessStatus decode(final boolean driveCycle, final int a, final int b, final int c, final int d) {
		final boolean compression = (b & 0x08) != 0;
		final List<ReadinessMonitor> monitors = new ArrayList<>();

		for (int bit = 0; bit < CONTINUOUS.length; bit++) {
			monitors.add(monitor(CONTINUOUS[bit], (b & (1 << bit)) != 0, (b & (1 << (bit + 4))) != 0));
		}

		final ReadinessMonitorId[] nonContinuous = compression ? COMPRESSION : SPARK;
		for (int bit = 0; bit < 8; bit++) {
			if (nonContinuous[bit] != null) {
				monitors.add(monitor(nonContinuous[bit], (c & (1 << bit)) != 0, (d & (1 << bit)) != 0));
			}
		}

		final boolean milOn = !driveCycle && (a & 0x80) != 0;
		final int dtcCount = driveCycle ? 0 : a & 0x7F;
		return new ReadinessStatus(milOn, dtcCount, compression, monitors);
	}

	private static ReadinessMonitor monitor(final ReadinessMonitorId id, final boolean supported,
			final boolean incomplete) {
		return new ReadinessMonitor(id, supported, supported && !incomplete);
	}
}
