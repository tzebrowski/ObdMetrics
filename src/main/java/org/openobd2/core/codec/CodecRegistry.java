package org.openobd2.core.codec;

import java.util.Optional;

import org.openobd2.core.command.Command;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(@NonNull PidRegistry pids, @NonNull String equationEngine) {
		return new DefaultRegistry(FormulaEvaluator.builder().pids(pids).engine(equationEngine).build());
	}
}