package org.obd.metrics.raw;

@FunctionalInterface
public interface DecimalReceiver {
	void receive(int pos, int dec);
}