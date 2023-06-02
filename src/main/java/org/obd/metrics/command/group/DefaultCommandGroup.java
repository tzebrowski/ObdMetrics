package org.obd.metrics.command.group;

import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCommandGroup<T extends Command> extends CommandGroup<T> {

	public static final CommandGroup<Command> INIT = new DefaultCommandGroup<>().of(
			new ATCommand("D"), // Set all to defaults
			new ATCommand("Z"), // Reset OBD
			new DelayCommand(0),
			new ATCommand("L0"), // Line feed off
			new ATCommand("H0"), // Headers off
			new ATCommand("E0"), // Echo off
			new ATCommand("PP 2CSV 01"), 
			new ATCommand("PP 2C ON"), // activate baud rate PP.
			new ATCommand("PP 2DSV 01"), // activate addressing pp.
			new ATCommand("PP 2D ON"),
			new ATCommand("AT2"));
}
