package org.openobd2.core.codec;

import java.util.Optional;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.group.Mode1CommandGroup;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(@NonNull PidRegistry pids) {
		final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pids).build();
		final DefaultRegistry registry = new DefaultRegistry(formulaEvaluator);
		Mode1CommandGroup.SUPPORTED_PIDS.getCommands().forEach(c -> registry.register(c, c));
		return registry;
	}
}