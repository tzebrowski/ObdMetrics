package org.obd.metrics.codec;

import java.util.Optional;

import org.obd.metrics.command.Command;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(@NonNull String equationEngine, boolean generator) {
		Codec<Number> evaluator = FormulaEvaluator.builder().engine(equationEngine).build();

		if (generator) {
			evaluator = new Generator(evaluator);
		}

		return new DefaultRegistry(evaluator);
	}
}