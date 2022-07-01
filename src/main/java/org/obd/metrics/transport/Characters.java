package org.obd.metrics.transport;

public interface Characters {

	static final String NORMALIZATION_PATTERN = "[a-zA-Z0-9]{1}\\:";
	static final boolean allowAllCharacters = false;

	static boolean isCharacterAllowed(final char character) {
		if (allowAllCharacters) {
			return true;
		}

		return character != '\t' && character != '\n' && character != '\r' && character != ' ';
	}

	static String normalize(final String message) {
		return message.replaceAll(NORMALIZATION_PATTERN, "");
	}
}
