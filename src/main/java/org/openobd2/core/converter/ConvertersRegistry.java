package org.openobd2.core.converter;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.mode1.Mode1Command;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.pid.PidDefinitionRegistry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConvertersRegistry {

	private final Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>();
	private final FormulaEvaluator formulaEvaluator;

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
		final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();
		final ConvertersRegistry convertersRegistry = new ConvertersRegistry(formulaEvaluator);
		convertersRegistry.registerMode1Commands();

		return convertersRegistry;
	}

	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	public Optional<Converter<?>> findConverter(Command command) {
		Converter<?> converter = registry.get(command);
		if (null == converter) {
			// no dedicated converter
			converter = formulaEvaluator;
		}
		return Optional.ofNullable(converter);
	}
}
