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
package org.obd.metrics.codec.mode1;

import static org.obd.metrics.api.model.ReadinessMonitorId.*;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.ReadinessMonitor;
import org.obd.metrics.api.model.ReadinessMonitorId;
import org.obd.metrics.api.model.ReadinessStatus;
import org.obd.metrics.command.ReadinessMonitorsCodec;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class ReadinessMonitorsTest {

	private static final long SINCE_CLEAR_ID = 21010L;
	private static final long DRIVE_CYCLE_ID = 21011L;

	private final PidDefinitionRegistry registry = PIDsRegistryFactory.get("mode01.json");
	private final ReadinessMonitorsCodec codec = new ReadinessMonitorsCodec();

	private ReadinessStatus decode(long id, String raw) {
		return codec.decode(registry.findBy(id), ConnectorResponseFactory.wrap(raw.getBytes()));
	}

	private static List<ReadinessMonitorId> ids(ReadinessStatus status, boolean supported, boolean complete) {
		return status.getMonitors().stream().filter(m -> m.isSupported() == supported && m.isComplete() == complete)
				.map(ReadinessMonitor::getId).collect(Collectors.toList());
	}

	@Test
	public void registeredWithCodecAndCategory() {
		Assertions.assertThat(registry.findBy(SINCE_CLEAR_ID).getPid()).isEqualTo("01");
		Assertions.assertThat(registry.findBy(SINCE_CLEAR_ID).getCategory()).isEqualTo("readiness");
		Assertions.assertThat(registry.findBy(DRIVE_CYCLE_ID).getPid()).isEqualTo("41");
	}

	@Test
	public void sparkIgnitionAllComplete() {
		// B=07 continuous supported + complete; C=E5 catalyst, evap, O2, O2 heater, EGR; D=00 nothing incomplete
		final ReadinessStatus status = decode(SINCE_CLEAR_ID, "41010007E500");
		Assertions.assertThat(status.isMilOn()).isFalse();
		Assertions.assertThat(status.getEmissionDtcCount()).isZero();
		Assertions.assertThat(status.isCompressionIgnition()).isFalse();
		Assertions.assertThat(ids(status, true, true)).containsExactly(MISFIRE, FUEL_SYSTEM, COMPONENTS, CATALYST,
				EVAPORATIVE_SYSTEM, OXYGEN_SENSOR, OXYGEN_SENSOR_HEATER, EGR_VVT);
		Assertions.assertThat(ids(status, false, false)).containsExactly(HEATED_CATALYST, SECONDARY_AIR,
				AC_REFRIGERANT);
	}

	@Test
	public void milOnCodeCountAndIncompleteMonitors() {
		// A=83 MIL on, 3 codes; D=E1 catalyst, O2, O2 heater, EGR incomplete
		final ReadinessStatus status = decode(SINCE_CLEAR_ID, "41018307E5E1");
		Assertions.assertThat(status.isMilOn()).isTrue();
		Assertions.assertThat(status.getEmissionDtcCount()).isEqualTo(3);
		Assertions.assertThat(ids(status, true, false)).containsExactly(CATALYST, OXYGEN_SENSOR,
				OXYGEN_SENSOR_HEATER, EGR_VVT);
		Assertions.assertThat(ids(status, true, true)).containsExactly(MISFIRE, FUEL_SYSTEM, COMPONENTS,
				EVAPORATIVE_SYSTEM);
	}

	@Test
	public void continuousMonitorIncomplete() {
		// B=27: continuous supported, bit 5 = fuel system incomplete
		final ReadinessStatus status = decode(SINCE_CLEAR_ID, "4101002700" + "00");
		Assertions.assertThat(ids(status, true, false)).containsExactly(FUEL_SYSTEM);
	}

	@Test
	public void compressionIgnitionUsesDieselMonitorsAndSkipsReservedBits() {
		// B=0F diesel; C=6B NMHC, NOx/SCR, boost, exhaust gas sensor, PM filter; D=40 PM filter incomplete
		final ReadinessStatus status = decode(SINCE_CLEAR_ID, "4101000F6B40");
		Assertions.assertThat(status.isCompressionIgnition()).isTrue();
		Assertions.assertThat(status.getMonitors()).hasSize(3 + 6);
		Assertions.assertThat(ids(status, true, true)).containsExactly(MISFIRE, FUEL_SYSTEM, COMPONENTS,
				NMHC_CATALYST, NOX_SCR, BOOST_PRESSURE, EXHAUST_GAS_SENSOR);
		Assertions.assertThat(ids(status, true, false)).containsExactly(PM_FILTER);
		Assertions.assertThat(ids(status, false, false)).containsExactly(EGR_VVT);
	}

	@Test
	public void driveCycleIgnoresByteA() {
		final ReadinessStatus status = decode(DRIVE_CYCLE_ID, "41418307E500");
		Assertions.assertThat(status.isMilOn()).isFalse();
		Assertions.assertThat(status.getEmissionDtcCount()).isZero();
		Assertions.assertThat(ids(status, true, true)).contains(CATALYST);
	}

	@Test
	public void secondEcuReplyIsIgnored() {
		final ReadinessStatus status = decode(SINCE_CLEAR_ID, "41018107E5004101000700" + "00");
		Assertions.assertThat(status.getEmissionDtcCount()).isEqualTo(1);
	}

	@Test
	public void shortOrMissingReplyDecodesToNull() {
		Assertions.assertThat(decode(SINCE_CLEAR_ID, "41010007")).isNull();
		Assertions.assertThat(decode(SINCE_CLEAR_ID, "NODATA")).isNull();
	}
}
