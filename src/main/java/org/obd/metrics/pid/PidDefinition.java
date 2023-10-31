/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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
package org.obd.metrics.pid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "id" })
public final class PidDefinition implements Comparable<PidDefinition> {
	
	public static class Overrides {
		@Getter
		private String canMode = "";
		
		@Getter
		private boolean batchEnabled = Boolean.TRUE;
	}

	public static class Historgam {

		@Getter
		private boolean minEnabled = Boolean.TRUE;

		@Getter
		private boolean maxEnabled = Boolean.TRUE;

		
		@Getter
		private boolean avgEnabled = Boolean.TRUE;
	}
	
	
	public static class Alert {
		@Setter
		@Getter
		private Number upperThreshold;	
			
		@Setter
		@Getter
		private Number lowerThreshold;	
	}

	protected static final int SUCCCESS_CODE = 40;
	
	@Getter
	@NonNull
	private Long id;

	@Getter
	@NonNull
	private int length;

	@Getter
	@NonNull
	private String formula;

	@Getter
	@NonNull
	private String mode;

	@Getter
	@NonNull
	private String pid;

	@Getter
	@NonNull
	private String units;

	@Getter
	@NonNull
	private String description;

	@Getter
	@NonNull
	private Number min;

	@Getter
	@NonNull
	private Number max;

	@Getter
	@NonNull
	private ValueType type;

	@Getter
	@Setter
	private Integer priority = 2;

	@Getter
	private CommandType commandType = CommandType.OBD;

	@Getter
	private String longDescription;

	@Getter
	private Boolean cacheable = Boolean.TRUE;

	@Getter
	private Boolean stable = Boolean.TRUE;

	@Getter
	@Setter
	private String resourceFile;

	@Setter
	@Getter
	private PIDsGroup group;

	@Getter
	private String commandClass;

	@Setter
	private String successCode;
	
	private String query;

	private byte[] successAnswerCodeBytes;
	
	private String predictedSuccessResponseCode;
	
	@Getter
	private Overrides overrides = new Overrides();
	
	@Getter
	private Historgam historgam = new Historgam();
	
	@Getter
	private Alert alert = new Alert();

	public boolean isMultiSegmentAnswer() {
		return length > 3;
	}
	
	public byte[] getSuccessCodeBytes() {
		if (successAnswerCodeBytes == null) {
			successAnswerCodeBytes = getSuccessCode().getBytes();
		}
		
		return successAnswerCodeBytes;
	}

	public String getSuccessCode() {
		if (successCode == null) {
			if (CommandType.OBD.equals(getCommandType())) {
				if (length == 4) {
					successCode = ("00C0:" + String.valueOf(SUCCCESS_CODE + Integer.valueOf(getMode())) + getPid())
							.toUpperCase();
					
				} else {
					// success code = 0x40 + mode + pid
					successCode = (String.valueOf(SUCCCESS_CODE + Integer.valueOf(getMode())) + getPid())
							.toUpperCase();
				}
			} else {
				successCode = getQuery().toUpperCase();
			}
		}
		return successCode;
	}
	
	public String getPredictedSuccessCode() {
		if (predictedSuccessResponseCode == null) {
			predictedSuccessResponseCode = String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
		}
		return predictedSuccessResponseCode;
	}
	
	public String getQuery() {
		if (query == null) {
			query = mode + pid;
		}
		return query;
	}

	public boolean isFormulaAvailable() {
		return formula != null && formula.length() > 0;
	}

	@Override
	public int compareTo(PidDefinition o) {
		return o.priority.compareTo(this.priority);
	}
}
