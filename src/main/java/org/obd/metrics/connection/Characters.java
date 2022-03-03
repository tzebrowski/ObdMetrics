package org.obd.metrics.connection;

public interface Characters {

	static final String FILTER_PATTERN = "SEARCHING...";
	static final String NORMALIZATION_PATTERN = "[a-zA-Z0-9]{1}\\:";

	static boolean isCharacterAllowed(char character) {
		return character != '\t' &&
		        character != '\n' &&
		        character != '\r' &&
		        character != ' ';
	}

	static String normalize(String message) {
		return message.replace(FILTER_PATTERN, "").replaceAll(NORMALIZATION_PATTERN, "");
	}
}
