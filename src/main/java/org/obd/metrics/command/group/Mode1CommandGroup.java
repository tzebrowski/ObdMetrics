package org.obd.metrics.command.group;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.at.DescribeProtocolCommand;
import org.obd.metrics.command.at.EchoCommand;
import org.obd.metrics.command.at.HeadersCommand;
import org.obd.metrics.command.at.LineFeedCommand;
import org.obd.metrics.command.at.LoadDefaultsCommand;
import org.obd.metrics.command.at.ResetCommand;
import org.obd.metrics.command.at.SelectProtocolCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.command.process.DelayCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Mode1CommandGroup<T extends Command> extends CommandGroup<T> {
	
	public static final CommandGroup<Command> INIT_NO_DELAY = new Mode1CommandGroup<Command>().of(
			new ResetCommand(),
			new LoadDefaultsCommand(),
			new LineFeedCommand(0), 
			new HeadersCommand(0), 
			new EchoCommand(0), 
			new SelectProtocolCommand("0"),
			new DescribeProtocolCommand());
	
	public static final CommandGroup<Command> INIT = new Mode1CommandGroup<Command>()
			.of(INIT_NO_DELAY)
			.of(new DelayCommand(5000));
	
	public static final CommandGroup<SupportedPidsCommand> SUPPORTED_PIDS = new Mode1CommandGroup<SupportedPidsCommand>().of(
			new SupportedPidsCommand("00"), 
			new SupportedPidsCommand("20"), 
			new SupportedPidsCommand("40"),
			new SupportedPidsCommand("60"), 
			new SupportedPidsCommand("80"), 
			new SupportedPidsCommand("A0"),
			new SupportedPidsCommand("C0"));
}
