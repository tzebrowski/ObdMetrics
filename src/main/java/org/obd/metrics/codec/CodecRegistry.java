package org.obd.metrics.codec;

import org.obd.metrics.api.Context.Service;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.formula.FormulaEvaluatorCodec;
import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;

public interface CodecRegistry extends Service{

	void register(PidDefinition pid, Codec<?> codec);

	Codec<?> findCodec(PidDefinition pid);

	@Builder
	public static DefaultRegistry of(final String equationEngine, final Adjustments adjustments) {
		Codec<Number> evaluator = FormulaEvaluatorCodec.instance(equationEngine, adjustments);

		if (adjustments != null && adjustments.getGenerator() != null && adjustments.getGenerator().isEnabled()) {
			evaluator = new Generator(evaluator, adjustments.getGenerator());
		}

		return new DefaultRegistry(evaluator);
	}
}