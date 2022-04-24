package org.obd.metrics.command.group;

import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AlfaMed17CommandGroup<T extends Command> extends CommandGroup<T> {
	public static final CommandGroup<Command> INIT = new CommandGroup<>().of(
	        new ATCommand("Z"), // reset
	        new ATCommand("L0"), // line feed off
	        new ATCommand("H0"), // headers off
	        new ATCommand("E0")); // echo off
//	        new ATCommand("PP 2CSV 01"),
//	        new ATCommand("PP 2C ON"), // activate baud rate PP.
//	        new ATCommand("PP 2DSV 01"), // activate addressing pp.
//	        new ATCommand("PP 2D ON"),
//	        new ATCommand("S0"), // Print spaces on/off
//	        new ATCommand("SP7")); // SAE J1939, CAN (ISO 15765-4)
//	        new ATCommand("CP18"), // Set CAN priority to 18 (29 bit only)
//	        new ATCommand("CRA 18DAF110"), // Set CAN hardware filter,18DAF110
//	        new ATCommand("SH DA10F1")); // Set CAN request message header: DA10F1
//	        new ATCommand("AT0"), // Adaptive timing off, auto1*, auto2
//	        new ATCommand("ST19"), // Set OBD response timeout.
//	        new ObdCommand(new PidDefinition(100002l, 0, "", "10", "03", "", "", 0, 0, PidDefinition.ValueType.DOUBLE))); // 50
	                                                                                                                 // 03
	                                                                                                                 // 003201F4
	// 3E00. keep the session open
}
