package org.openobd2.core.converter;

import java.util.Optional;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.mode1.Mode1Command;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;

public interface ConverterRegistry {

	void register(Command command, Converter<?> converter);

	Optional<Converter<?>> findConverter(Command command);

	
	@Builder
	public static DefaultRegistry registry(@NonNull PidRegistry pidRegistry) {
		final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();
		final DefaultRegistry convertersRegistry = new DefaultRegistry(formulaEvaluator);
		for (final Mode1Command<?> command : new Mode1Command[] { 
				new SupportedPidsCommand("00"),
				new SupportedPidsCommand("02"), 
				new SupportedPidsCommand("04"), 
				new SupportedPidsCommand("06") }) {
			convertersRegistry.register(command, command);
		}
		
		
		return convertersRegistry;
	}

}