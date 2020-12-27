package org.openobd2.core.codec;

import java.util.Optional;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry registry(@NonNull PidRegistry pidRegistry) {
		final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();
		final DefaultRegistry registry = new DefaultRegistry(formulaEvaluator);
		for (final SupportedPidsCommand command : 
				new SupportedPidsCommand[] { 
						new SupportedPidsCommand("00"),
						new SupportedPidsCommand("02"), 
						new SupportedPidsCommand("04"), 
						new SupportedPidsCommand("06") }) {
			registry.register(command, command);
		}

		return registry;
	}

}