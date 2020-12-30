package org.openobd2.core.command;

import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.at.DescribeProtocolCommand;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.at.SelectProtocolCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public  class CommandSet<T extends Command> {

	@Getter
	protected LinkedBlockingDeque<T> commands = new LinkedBlockingDeque<T>();

	public static final CommandSet<Command> INIT_PROTO_DEFAULT = of(
			new ResetCommand(),
			new LineFeedCommand(0), 
			new HeadersCommand(0), 
			new EchoCommand(0), 
			new SelectProtocolCommand(0),
			new DescribeProtocolCommand());

	public static final CommandSet<SupportedPidsCommand> MODE1_SUPPORTED_PIDS = of(
			new SupportedPidsCommand("00"), 
			new SupportedPidsCommand("20"), 
			new SupportedPidsCommand("40"),
			new SupportedPidsCommand("60"), 
			new SupportedPidsCommand("80"), 
			new SupportedPidsCommand("A0"),
			new SupportedPidsCommand("C0"));

	

	@SuppressWarnings("unchecked")
	protected static <T extends Command> CommandSet<T> of(T... commands) {
		final CommandSet<T> cs = new CommandSet<T>();
		for (T command : commands) {
			cs.commands.add(command);
		}
		return cs;
	}
}
