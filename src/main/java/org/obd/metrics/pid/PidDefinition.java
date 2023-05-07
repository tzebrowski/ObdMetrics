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
