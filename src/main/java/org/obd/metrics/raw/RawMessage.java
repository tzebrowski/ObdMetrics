package org.obd.metrics.raw;

public interface RawMessage {

	String getMessage();

	boolean isNoData();

	boolean isError();

	static RawMessage instance(String message) {
		return new RawStringMessage(message);
	}
}
