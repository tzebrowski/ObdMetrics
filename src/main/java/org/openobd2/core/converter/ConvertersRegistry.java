package org.openobd2.core.converter;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.mode1.CustomCommand;
import org.openobd2.core.command.obd.mode1.EngineRpmCommand;
import org.openobd2.core.command.obd.mode1.EngineTempCommand;
import org.openobd2.core.command.obd.mode1.Mode1Command;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.command.obd.mode1.VehicleSpeedCommand;

public final class ConvertersRegistry {

	final Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>();

	public ConvertersRegistry(){
		registerMode1Commands();
	}
	
	void registerMode1Commands() {
		for (final Mode1Command<?> command : new Mode1Command[] { 
				new EngineTempCommand(), new EngineRpmCommand(),
				new VehicleSpeedCommand(), new CustomCommand("0e"), 
				new CustomCommand("0f"), new CustomCommand("04"),
				new CustomCommand("0b"), new CustomCommand("1c"), 
				new CustomCommand("01"), new CustomCommand("11"),
				 new CustomCommand("10"),new SupportedPidsCommand("00") }) {
			register(command,command);
		}
	}

	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	public Optional<Converter<?>> findConverter(Command command) {
		return Optional.ofNullable(registry.get(command));
	}
}
