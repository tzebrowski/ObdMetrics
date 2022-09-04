package org.obd.metrics.command.group;

import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCommandGroup<T extends Command> extends CommandGroup<T> {

	public static final CommandGroup<Command> INIT = new DefaultCommandGroup<>().of(
			new ATCommand("D"), // Set all to defaults
			new ATCommand("Z"), // Reset OBD
			new ATCommand("L0"), // Line feed off
			new ATCommand("H0"), // Headers off
			new ATCommand("E0"), // Echo off
			new ATCommand("PP 2CSV 01"), 
			new ATCommand("PP 2C ON"), // activate baud rate PP.
			new ATCommand("PP 2DSV 01"), // activate addressing pp.
			new ATCommand("PP 2D ON"),
			new ATCommand("AT2"));

	public static final CommandGroup<SupportedPidsCommand> SUPPORTED_PIDS = 
			new DefaultCommandGroup<SupportedPidsCommand>().of(
					new SupportedPidsCommand(100001l, "00"), 
					new SupportedPidsCommand(100002l, "20"),
					new SupportedPidsCommand(100003l, "40"), 
					new SupportedPidsCommand(100004l, "60"),
					new SupportedPidsCommand(100005l, "80"), 
					new SupportedPidsCommand(100006l, "A0"),
					new SupportedPidsCommand(100007l, "C0"));
}
