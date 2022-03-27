package org.obd.metrics.codec;

import org.obd.metrics.api.Adjustments;
import org.obd.metrics.codec.formula.FormulaEvaluatorCodec;
import org.obd.metrics.command.Command;

import lombok.Builder;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Codec<?> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(String equationEngine, Adjustments adjustments) {
		Codec<Number> evaluator = FormulaEvaluatorCodec.instance(equationEngine, adjustments);

		if (adjustments != null &&
		        adjustments.getGenerator() != null &&
		        adjustments.getGenerator().isEnabled()) {
			evaluator = new Generator(evaluator, adjustments.getGenerator());
		}

		return new DefaultRegistry(evaluator);
	}
}