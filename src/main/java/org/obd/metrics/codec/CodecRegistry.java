package org.obd.metrics.codec;

import java.util.Optional;

import org.obd.metrics.command.Command;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(@NonNull String equationEngine, boolean enableGenerator, Double generatorIncrement) {
		Codec<Number> evaluator = FormulaEvaluator.builder().engine(equationEngine).build();

		if (enableGenerator) {
			evaluator = new Generator(evaluator, generatorIncrement == null ? 5.0 : generatorIncrement);
		}

		return new DefaultRegistry(evaluator);
	}
}