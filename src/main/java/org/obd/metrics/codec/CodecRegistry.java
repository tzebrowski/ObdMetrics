package org.obd.metrics.codec;

import org.obd.metrics.command.Command;

import lombok.Builder;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Codec<?> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(String equationEngine, GeneratorSpec generatorSpec) {
		Codec<Number> evaluator = new FormulaEvaluator(equationEngine);

		if (generatorSpec != null && generatorSpec.isEnabled()) {
			evaluator = new Generator(evaluator, generatorSpec);
		}

		return new DefaultRegistry(evaluator);
	}
}