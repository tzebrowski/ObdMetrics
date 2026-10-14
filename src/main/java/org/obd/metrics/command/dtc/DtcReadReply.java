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

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;

/**
 * The decoded codes of one DTC read reply, plus how the module answered. A plain list for every consumer; the DTC
 * handler reads {@link #getStatus()} to tell a refusal (empty list, {@link DtcModuleStatus#REFUSED}) from "no codes".
 * The status cannot come from the reply's raw response: that is published only with raw collection enabled.
 */
public final class DtcReadReply extends ArrayList<org.obd.metrics.api.model.DiagnosticTroubleCode> {

	private static final long serialVersionUID = 1L;

	@Getter
	private final DtcModuleStatus status;

	public DtcReadReply(final DtcModuleStatus status, final Collection<org.obd.metrics.api.model.DiagnosticTroubleCode> codes) {
		super(codes);
		this.status = status;
	}
}
