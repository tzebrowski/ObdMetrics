package org.obd.metrics.codec;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.formula.FormulaEvaluatorCodec;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.context.Service;
import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;

public interface CodecRegistry extends Service {

	void register(PidDefinition pid, Codec<?> codec);

	Codec<?> findCodec(PidDefinition pid);

	@Builder
	public static DefaultRegistry of(final FormulaEvaluatorConfig formulaEvaluatorConfig,
			final Adjustments adjustments) {

		Codec<Number> evaluator = FormulaEvaluatorCodec.instance(formulaEvaluatorConfig, adjustments);

		if (adjustments != null && adjustments.getGeneratorPolicy() != null && adjustments.getGeneratorPolicy().isEnabled()) {
			evaluator = new Generator(evaluator, adjustments.getGeneratorPolicy());
		}

		return new DefaultRegistry(evaluator);
	}
}