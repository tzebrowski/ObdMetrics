package org.obd.metrics.command.group;

import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.DeviceProperty;
import org.obd.metrics.command.VinCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Mode1CommandGroup<T extends Command> extends CommandGroup<T> {

	public static final CommandGroup<Command> INIT = new Mode1CommandGroup<Command>().of(
	        new ATCommand("Z"), // reset
	        new ATCommand("L0"), // line feed off
	        new ATCommand("H0"), // headers off
	        new ATCommand("E0"), // echo off
	        new ATCommand("SP0"), // select protocol auto
	        new DeviceProperty("AT I", "The device ID"), // elm info
	        new DeviceProperty("AT @1", "Device description"), // device description
	        new DeviceProperty("AT @2", "Device information"), // device information
	        new DeviceProperty("AT DP", "Selected protocol"), // describe protocol
	        new DeviceProperty("AT DPN", "Selected protocol by number"), // describe protocol by number
	        new DeviceProperty("AT RV", "Battery voltage"), //battery voltage
	        new VinCommand());

	public static final CommandGroup<SupportedPidsCommand> SUPPORTED_PIDS = new Mode1CommandGroup<SupportedPidsCommand>()
	        .of(
	                new SupportedPidsCommand(100001l,"00"),
	                new SupportedPidsCommand(100002l,"20"),
	                new SupportedPidsCommand(100003l,"40"),
	                new SupportedPidsCommand(100004l,"60"),
	                new SupportedPidsCommand(100005l,"80"),
	                new SupportedPidsCommand(100006l,"A0"),
	                new SupportedPidsCommand(100007l,"C0"));
}
