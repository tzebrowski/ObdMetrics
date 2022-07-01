package org.obd.metrics.codec.formula;

import org.obd.metrics.api.Adjustments;
import org.obd.metrics.codec.Codec;

public interface FormulaEvaluatorCodec extends Codec<Number> {

	static FormulaEvaluatorCodec instance(final String engine, final Adjustments adjustments) {
		return new FormulaEvaluator(engine, adjustments);
	}
}
