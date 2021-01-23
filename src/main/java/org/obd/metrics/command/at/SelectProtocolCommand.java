package org.obd.metrics.command.at;

public final class SelectProtocolCommand extends ATCommand {
	public SelectProtocolCommand(String value) {
		super("SP" + value, "Select protocol");
	}
}
