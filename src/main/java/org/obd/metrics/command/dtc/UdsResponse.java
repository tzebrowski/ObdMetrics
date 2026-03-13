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
import java.util.List;

import org.obd.metrics.api.model.DiagnosticTroubleCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UdsResponse {

	private String rawPayload;
	private String statusAvailabilityMaskHex;
	private List<String> supportedStatuses;
	private final List<DiagnosticTroubleCode> dtcs = new ArrayList<DiagnosticTroubleCode>();
	private String errorMessage;
	
	public void addDiagnosticTroubleCode(DiagnosticTroubleCode dtc) {
		dtcs.add(dtc);
	}
    
    public boolean hasError() { return errorMessage != null; }
}