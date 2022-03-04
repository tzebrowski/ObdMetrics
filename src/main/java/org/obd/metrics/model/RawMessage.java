package org.obd.metrics.model;

public interface RawMessage {

	String getMessage();

	default boolean isNoData() {
		return false;
	}

	default boolean isError() {
		return false;
	}	

	static RawMessage instance(String message) {
		return new StringMessage(message);
	}
}
