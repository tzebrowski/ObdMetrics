package org.obd.metrics.raw;

@FunctionalInterface
public interface DecimalHandler {
	void handle(int pos, int dec);
}