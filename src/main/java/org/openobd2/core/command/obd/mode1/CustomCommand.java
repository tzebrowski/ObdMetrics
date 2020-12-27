package org.openobd2.core.command.obd.mode1;

public final class CustomCommand extends Mode1Command<Long> {

	
	public CustomCommand(String pid) {
		super(pid, "Custom command: " + pid);
	}

	@Override
	public Long convert(String raw) {
		if (isSuccessAnswerCode(raw)) {
			return getDecimalAnswerData(raw);
		}
		return null;
	}
}
