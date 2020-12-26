package org.openobd2.core.converter;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.mode1.Mode1Command;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConvertersRegistry {

	final Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>();
	private ConverterEngine converterEngine;


	void registerMode1Commands() {
		for (final Mode1Command<?> command : new Mode1Command[] { 
				new SupportedPidsCommand("00"),
				new SupportedPidsCommand("02"), 
				new SupportedPidsCommand("04"), 
				new SupportedPidsCommand("06") }) {
			register(command, command);
		}
	}
	
	@Builder
	public static ConvertersRegistry  registry() {
		final ConvertersRegistry convertersRegistry = new ConvertersRegistry();
		
		convertersRegistry.registerMode1Commands();
		
		final String definitionFile = "definitions.json";
		convertersRegistry.converterEngine = ConverterEngine.builder().definitionFile(definitionFile).build();
		
		return convertersRegistry;
	}

	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	public Optional<Converter<?>> findConverter(Command command) {
		Converter<?> converter = registry.get(command);
		if (null == converter) {
			//no dedicated converter
			converter = converterEngine;
		}
		return Optional.ofNullable(converter);
	}
}
