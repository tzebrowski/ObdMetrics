package org.obd.metrics.api;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
final class ParametersValidator {

	static void ensureCommandFreqIsValid(Integer commandFrequency) {
		log.debug("Checking command frequency: {}", commandFrequency);
		if (commandFrequency != null && (commandFrequency < 1 || commandFrequency > 15)) {
			log.debug("Invalid command frequency. Value must be between 1 and 15");
			throw new IllegalArgumentException("Command frequency must be between 1 and 15");
		}
	}
}
