package org.openobd2.core;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.Converter;
import org.openobd2.core.command.EngineTempCommand;
import org.openobd2.core.command.SupportedPidsCommand;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ConvertersRegistry {

	Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>() {
		private static final long serialVersionUID = 1L;

		{
			put(new EngineTempCommand(), new EngineTempCommand());
			put(new SupportedPidsCommand("00"), new SupportedPidsCommand("00"));
		}
	};

	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	public Optional<Converter<?>> findConverter(Command command) {
		return Optional.ofNullable(registry.get(command));
	}
}
