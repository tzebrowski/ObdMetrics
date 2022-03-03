package org.obd.metrics.raw;

public interface Raw {

	String getMessage();
	
	static Raw instance(String message) {
		return new RawString(message);
	}
}
