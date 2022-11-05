package org.obd.metrics.transport.message;

@FunctionalInterface
public interface DecimalReceiver {
	void receive(int pos, int dec);
}