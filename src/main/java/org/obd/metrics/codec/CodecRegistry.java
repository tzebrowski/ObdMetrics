package org.obd.metrics.codec;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.formula.FormulaEvaluatorCodec;
import org.obd.metrics.context.Service;
import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;

public interface CodecRegistry extends Service{

	void register(PidDefinition pid, Codec<?> codec);

	Codec<?> findCodec(PidDefinition pid);

	@Builder
	public static DefaultRegistry of(final String equationEngine, final Adjustments adjustments) {
		final String engine = equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
		
		Codec<Number> evaluator = FormulaEvaluatorCodec.instance(engine, adjustments);

		if (adjustments != null && adjustments.getGenerator() != null && adjustments.getGenerator().isEnabled()) {
			evaluator = new Generator(evaluator, adjustments.getGenerator());
		}

		return new DefaultRegistry(evaluator);
	}
}