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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Where a scheduled DTC action is. {@link Phase#MODULE}: the requests to {@link #getModule()} are about to be sent,
 * the {@link #getIndex()}-th (1-based) of {@link #getTotal()} modules. {@link Phase#FOLLOW_UPS}: all modules were read
 * and the per-code requests (snapshots, follow-ups) for {@link #getTotal()} codes are about to be sent; module is null.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class DtcProgress {

	public enum Phase {
		MODULE, FOLLOW_UPS
	}

	private final Phase phase;
	private final String module;
	private final int index;
	private final int total;

	public static DtcProgress module(final String module, final int index, final int total) {
		return new DtcProgress(Phase.MODULE, module, index, total);
	}

	public static DtcProgress followUps(final int codes) {
		return new DtcProgress(Phase.FOLLOW_UPS, null, 0, codes);
	}
}
