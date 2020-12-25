package org.openobd2.core.command.obd;

import org.openobd2.core.command.Command;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class ObdFrame extends Command {

	@Getter
	private String mode;

	@Getter
	private String pid;

	public ObdFrame(final String mode, final String pid, final String label) {
		super(mode + pid, label);
		this.mode = mode;
		this.pid = pid;
	}

	// this is not good place for this
	protected boolean isSuccessAnswerCode(String raw) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode());
	}

	// this is not good place for this
	protected String getPredictedAnswerCode() {
		// success code = 0x40 + mode + pid
		return String.valueOf(40 + Integer.valueOf(getMode())) + getPid();
	}

	// this is not good place for this
	protected String getRawAnswerData(String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode().length());
	}

	// this is not good place for this
	protected Long getDecimalAnswerData(String raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(raw), 16);
	}
}
