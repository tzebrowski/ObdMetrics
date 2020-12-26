package org.openobd2.core.converter;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.mode1.Mode1Command;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.definition.PidDefinitionRegistry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConvertersRegistry {

	private final Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>();
	private DynamicFormulaEvaluator formulaEvaluator;

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
	public static ConvertersRegistry registry(@NonNull PidDefinitionRegistry pidRegistry) {
		final ConvertersRegistry convertersRegistry = new ConvertersRegistry();
		convertersRegistry.registerMode1Commands();
		convertersRegistry.formulaEvaluator = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry)
				.build();
		return convertersRegistry;
	}

	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	public Optional<Converter<?>> findConverter(Command command) {
		Converter<?> converter = registry.get(command);
		if (null == converter) {
			//no dedicated converter
			converter = formulaEvaluator;
		}
		return Optional.ofNullable(converter);
	}
}
