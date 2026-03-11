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

import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UdsSnapshotResponse {

	@Getter
	@RequiredArgsConstructor
	public static class ParsedDid {
        private final PidDefinition definition;
        private final String rawValueHex;
        private final Number decodedValue;

        @Override
        public String toString() {
            return String.format("DID: %s | Raw: %-8s | Decoded: %-6s %-5s | %s", 
                definition.getPid(), 
                rawValueHex, 
                decodedValue != null ? decodedValue : "N/A",
                definition.getUnits() != null ? definition.getUnits() : "",
                definition.getDescription()
            );
        }
    }
	
	
	private String dtcHex;
	private String statusHex;
	private int recordNumber;
	private int numberOfDids;
	private String rawDataBlock;

	private boolean isError = false;
	private String errorMessage = "";
	private final List<ParsedDid> extractedDids = new ArrayList<>();
	
	void addDid(ParsedDid did) {
		extractedDids.add(did);
	}
	void setError(String errorMessage) {
		this.isError = true;
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		if (isError) {
			return "Error: " + errorMessage;
		}
		return String.format("Snapshot [DTC=%s, Status=0x%s, Record=%d, DIDs=%d, DataBlock=%s]", dtcHex, statusHex,
				recordNumber, numberOfDids, rawDataBlock);
	}
}