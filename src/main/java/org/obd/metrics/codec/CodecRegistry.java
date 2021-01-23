package org.obd.metrics.codec;

import java.util.Optional;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidRegistry;

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