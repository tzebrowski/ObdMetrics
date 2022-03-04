package org.obd.metrics.model;

import java.util.Arrays;
import java.util.List;

import org.obd.metrics.connection.Characters;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class StringMessage implements RawMessage {

	private static final List<String> ERRORS = Arrays.asList("UNABLETOCONNECT", "STOPPED", "ERROR", "CANERROR",
	        "BUSINIT");

	@Getter
	private final boolean noData;
	
	@Getter
	private final boolean isError;

	@Getter
	private final String message;

	StringMessage(String in) {
		this.message = Characters.normalize(in.toString());
		this.noData = message == null || message.contains("nodata");
		this.isError = ERRORS.contains(message);
	}
}
