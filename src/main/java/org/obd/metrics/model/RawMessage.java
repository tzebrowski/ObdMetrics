package org.obd.metrics.model;

public interface RawMessage {

	String getMessage();

	default boolean isAnswerCodeSuccess(byte []expectedAnswer) {
		return true;
	}
	
	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}	

	static RawMessage instance(String message) {
		return new StringMessage(message);
	}
}
