/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.command.dtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeCommand extends Command implements Codec<List<DiagnosticTroubleCode>> {

	private static final String pattern = "[a-zA-Z0-9]{1}\\:";
	private static final int codeLength = 6;

	protected final PidDefinition pid;

	public DiagnosticTroubleCodeCommand(PidDefinition pid) {
		super(pid.getQuery(), pid.getService(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public List<DiagnosticTroubleCode> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		if (connectorResponse.isEmpty()) {
			return Collections.emptyList();
		} else {
			final Optional<List<DiagnosticTroubleCode>> decode = decode(connectorResponse.getMessage());
			if (decode.isPresent()) {
				final List<DiagnosticTroubleCode> codes = decode.get();
				if (log.isDebugEnabled()) {
					codes.forEach(dtc -> log.debug("Found DTC: {}", dtc));
				}
				return codes;
			}
		}
		return Collections.emptyList();
	}

	private Optional<List<DiagnosticTroubleCode>> decode(final String rx) {
		final String successCode = pid.getSuccessCode();
		final int successCodeIndex = rx.indexOf(successCode);
		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>();

		if (successCodeIndex >= 0) {
			final String codes = rx.substring(successCodeIndex + successCode.length())
					.replaceAll(pattern, "")
					.replaceAll("48", "");

			for (int i = 0; i < codes.length() / codeLength; i++) {
				final int beginIndex = i * codeLength;
				dtcList.add(DiagnosticTroubleCode.builder().code(codes.substring(beginIndex, beginIndex + codeLength)).build());
			}
			return Optional.of(dtcList);
		} else {
			return Optional.empty();
		}
	}
}
