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
package org.obd.metrics.command.dtc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

/**
 * Resolves the DIDs of a DTC snapshot (19 04) or of follow-up data (e.g. 19 06) to their definitions. A dtcSnapshot
 * definition wins over livedata: a freeze frame may store a DID in fewer bytes, or with another scaling, than its live
 * (22) reply. Without one, the livedata definition is used - mode 22 first, then the lowest id, so the choice does not
 * depend on registry iteration order. Definitions without a length are skipped: they cannot be stepped over.
 */
public final class DtcDidLookup {

	private final Map<String, PidDefinition> byDid = new HashMap<>();

	public DtcDidLookup(final PidDefinitionRegistry registry) {
		final Comparator<PidDefinition> preference = Comparator
				.comparing((PidDefinition p) -> "22".equals(p.getMode()) ? 0 : 1)
				.thenComparing(PidDefinition::getId);

		registry.findAll().stream().filter(p -> isDid(p)).sorted(preference)
				.forEach(p -> byDid.putIfAbsent(p.getPid().toUpperCase(), p));

		registry.findBy(PIDsGroup.DTC_SNAPSHOT).stream().filter(p -> isDid(p))
				.sorted(Comparator.comparing(PidDefinition::getId))
				.forEach(p -> byDid.merge(p.getPid().toUpperCase(), p,
						(existing, snapshot) -> existing.getGroup() == PIDsGroup.DTC_SNAPSHOT ? existing : snapshot));
	}

	public Optional<PidDefinition> find(final String did) {
		return did == null ? Optional.empty() : Optional.ofNullable(byDid.get(did.toUpperCase()));
	}

	private static boolean isDid(final PidDefinition p) {
		return p.getPid() != null && p.getPid().length() == 4 && p.getLength() > 0;
	}
}
