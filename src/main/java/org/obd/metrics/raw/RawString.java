package org.obd.metrics.raw;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "message")
@AllArgsConstructor(access = AccessLevel.PUBLIC)
final class RawString implements Raw {

	final String message;

	@Override
	public String getMessage() {
		return message;
	}
}
