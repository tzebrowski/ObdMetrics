package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.Codec;

public interface FormulaEvaluatorCodec extends Codec<Number> {

	static FormulaEvaluatorCodec instance(FormulaEvaluatorConfig formulaEvaluatorConfig, final Adjustments adjustments) {
		return new FormulaEvaluator(formulaEvaluatorConfig, adjustments);
	}
}
