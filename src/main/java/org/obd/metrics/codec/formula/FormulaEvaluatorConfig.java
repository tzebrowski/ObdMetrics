package org.obd.metrics.codec.formula;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FormulaEvaluatorConfig {

	@Default
	private final String scriptEngine = "JavaScript";

	@Default
	private final Boolean debug = false;
}
