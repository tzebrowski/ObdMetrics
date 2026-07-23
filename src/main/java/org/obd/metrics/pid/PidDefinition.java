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
package org.obd.metrics.pid;

import org.obd.metrics.api.CANNetwork;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
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
	
	private static final String DEFAULT_MODULE = "ecu";
	
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Overrides {
		@Getter
		private String canMode = "";
		
		@Getter
		private boolean batchEnabled = Boolean.TRUE;
		
		@Getter
        private CANNetwork canNetwork = CANNetwork.HS_CAN;
		
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
	
	public PidDefinition(long id, String pid, String query, String description, String mode, String codecClass) {
		this.pid = pid;
		this.id = id;
		this.query = query;
		this.codecClass = codecClass;
		this.mode = mode;
	}

	public PidDefinition(long id, int length, String formula, String mode, String pid, String units, String description,
			Number min, Number max, ValueType type) {
		this.id = id;
		this.length = length;
		this.formula = formula;
		this.mode = mode;
		this.pid = pid;
		this.units = units;
		this.description = description;
		this.min = min;
		this.max = max;
		this.type = type;
	}
	
	public PidDefinition(long id, int length, String formula, String mode, String pid, String units, String description,
			Number min, Number max, ValueType type, Overrides overrides) {
		this.id = id;
		this.length = length;
		this.formula = formula;
		this.mode = mode;
		this.pid = pid;
		this.units = units;
		this.description = description;
		this.min = min;
		this.max = max;
		this.type = type;
		this.overrides = overrides;
	}
	
	public PidDefinition(long id, String mode, String description,
			Number min, Number max, ValueType type, CommandType commandType) {
		this.id = id;
		this.length = 0;
		this.formula = "";
		this.mode = mode;
		this.pid = "";
		this.units = "";
		this.description = description;
		this.min = min;
		this.max = max;
		this.type = type;
		this.commandType = commandType;
	}
	
	protected static final int SUCCCESS_CODE = 40;
	
	@Getter
	@NonNull
	private Long id;

	@Setter
	@Getter
	@NonNull
	private int length;

	@Getter
	@Setter
	@NonNull
	private String formula;

	@Setter
	@Getter
	@NonNull
	private String mode;

	@Setter
	@Getter
	@NonNull
	private String pid;

	@Getter
	@Setter
	private String units;

	@Getter
	@Setter
	@NonNull
	private String description;

	@Setter
	@Getter
	private Number min;

	@Setter
	@Getter
	private Number max;

	@Setter
	@Getter
	private ValueType type;

	@Getter
	@Setter
	private Integer priority = 2;

	@Setter
	@Getter
	private CommandType commandType = CommandType.OBD;

	@Getter
	@Setter
	private String longDescription;

	@Setter
	@Getter
	private Boolean cacheable = Boolean.TRUE;

	@Setter
	@Getter
	private Boolean stable = Boolean.TRUE;

	@Getter
	@Setter
	private String resourceFile;

	@Setter
	@Getter
	private PIDsGroup group;

	@Setter
	@Getter
	private String codecClass;

	@Setter
	private String successCode;
	
	private String query;

	private byte[] successAnswerCodeBytes;
	
	private String predictedSuccessResponseCode;
	
	@Setter
	@Getter
	private Overrides overrides = new Overrides();
	
	@Getter
	private Historgam historgam = new Historgam();
	
	@Setter
	@Getter
	private Alert alert = new Alert();

	@Setter
	@Getter
	private String module = DEFAULT_MODULE;
	
	@Setter
	@Getter
	private boolean signed = false;

	@Setter
	@Getter
	private boolean formulaParameterSplitBinding = true;
	
	@JsonIgnore
	public CANNetwork getCanNetwork() {
        return getOverrides().getCanNetwork() != null 
                ? getOverrides().getCanNetwork() 
                : CANNetwork.HS_CAN;
    }
	
	/**
	 * Returns a shallow copy of this definition with {@code module}
	 * overridden. Used when the same DTC PID definition needs to be queried
	 * against several vehicle modules, each response tagged with the module
	 * it came from, without mutating the shared registry instance.
	 */
	@JsonIgnore
	public PidDefinition withModule(String module) {
		final PidDefinition copy = new PidDefinition();
		copy.id = this.id;
		copy.length = this.length;
		copy.formula = this.formula;
		copy.mode = this.mode;
		copy.pid = this.pid;
		copy.units = this.units;
		copy.description = this.description;
		copy.min = this.min;
		copy.max = this.max;
		copy.type = this.type;
		copy.priority = this.priority;
		copy.commandType = this.commandType;
		copy.longDescription = this.longDescription;
		copy.cacheable = this.cacheable;
		copy.stable = this.stable;
		copy.resourceFile = this.resourceFile;
		copy.group = this.group;
		copy.codecClass = this.codecClass;
		copy.successCode = this.successCode;
		copy.query = this.query;
		copy.successAnswerCodeBytes = this.successAnswerCodeBytes;
		copy.predictedSuccessResponseCode = this.predictedSuccessResponseCode;
		copy.overrides = this.overrides;
		copy.alert = this.alert;
		copy.signed = this.signed;
		copy.formulaParameterSplitBinding = this.formulaParameterSplitBinding;
		copy.module = module;
		return copy;
	}

	@JsonIgnore
	public String deductMode() {
		return getOverrides().getCanMode() != null && getOverrides().getCanMode().length() > 0
				? getOverrides().getCanMode()
				: mode;
	}
	
	@JsonIgnore
	public boolean isMultiSegmentAnswer() {
		return length > 3;
	}
	
	@JsonIgnore
	public byte[] getSuccessCodeBytes() {
		if (successAnswerCodeBytes == null) {
			successAnswerCodeBytes = getSuccessCode().getBytes();
		}
		
		return successAnswerCodeBytes;
	}

	public String getSuccessCode() {
		if (successCode == null) {
			if (CommandType.OBD.equals(getCommandType())) {
				// success code = 0x40 + mode + pid
				successCode = (String.valueOf(SUCCCESS_CODE + Integer.valueOf(getMode())) + getPid())
						.toUpperCase();
			} else {
				successCode = getQuery().toUpperCase();
			}
		}
		return successCode;
	}
	
	@JsonIgnore
	public String getPredictedSuccessCode() {
		if (predictedSuccessResponseCode == null) {
			predictedSuccessResponseCode = String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
		}
		return predictedSuccessResponseCode;
	}
	
	@JsonIgnore
	public String getQuery() {
		if (query == null) {
			query = mode + pid;
		}
		return query;
	}

	@JsonIgnore
	public boolean isFormulaAvailable() {
		return formula != null && formula.length() > 0;
	}

	@Override
	public int compareTo(PidDefinition o) {
		return o.priority.compareTo(this.priority);
	}
}
