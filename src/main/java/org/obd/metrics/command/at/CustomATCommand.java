package org.obd.metrics.command.at;

public class CustomATCommand extends ATCommand {
	public CustomATCommand(String value) {
		super(value, "Custom AT command: " + value);
	}
}
