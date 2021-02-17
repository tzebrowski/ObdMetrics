package org.obd.metrics.command.group;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.at.CustomATCommand;
import org.obd.metrics.command.at.EchoCommand;
import org.obd.metrics.command.at.HeadersCommand;
import org.obd.metrics.command.at.LineFeedCommand;
import org.obd.metrics.command.at.ResetCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AlfaMed17CommandGroup <T extends Command> extends CommandGroup<T> {
	public static final CommandGroup<Command> CAN_INIT_NO_DELAY = new CommandGroup<>().of(
			new ResetCommand(),
			new LineFeedCommand(0), 
			new HeadersCommand(0), 
			new EchoCommand(0), 
			new CustomATCommand("PP 2CSV 01"),
			new CustomATCommand("PP 2C ON"), // activate baud rate PP.
			new CustomATCommand("PP 2DSV 01"),// activate addressing pp.
			new CustomATCommand("PP 2D ON"),
			new CustomATCommand("S0"),// Print spaces on*/off
			new CustomATCommand("SPB"),// SAE J1939 CAN (29 bit ID, 250* kbaud)
			new CustomATCommand("CP18"),// Set CAN priority to 18 (29 bit only)
			new CustomATCommand("CRA 18DAF110"),// Set CAN hardware filter,18DAF110
			new CustomATCommand("SH DA10F1"),// Set CAN request message header: DA10F1
			new CustomATCommand("AT0"),// Adaptive timing off, auto1*, auto2
			new CustomATCommand("ST19"),// Set OBD response timeout.
			new ObdCommand(new PidDefinition(100002l,0, "", "10", "03", "", "", "", "",PidDefinition.Type.DOUBLE))); // 50 03 003201F4
			// 3E00. keep the session open
	
	public static final CommandGroup<Command> CAN_INIT = new CommandGroup<>()
			.of(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
			.of(new DelayCommand(5000)); // 50 03 003201F4
}
