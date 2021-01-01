package org.openobd2.core.command.group;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.at.DescribeProtocolCommand;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.at.SelectProtocolCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Mode1CommandGroup<T extends Command> extends CommandGroup<T> {

	public static final CommandGroup<Command> INIT_PROTO_DEFAULT = of(
			new ResetCommand(),
			new LineFeedCommand(0), 
			new HeadersCommand(0), 
			new EchoCommand(0), 
			new SelectProtocolCommand(0),
			new DescribeProtocolCommand());

	public static final CommandGroup<SupportedPidsCommand> SUPPORTED_PIDS = of(
			new SupportedPidsCommand("00"), 
			new SupportedPidsCommand("20"), 
			new SupportedPidsCommand("40"),
			new SupportedPidsCommand("60"), 
			new SupportedPidsCommand("80"), 
			new SupportedPidsCommand("A0"),
			new SupportedPidsCommand("C0"));
}
