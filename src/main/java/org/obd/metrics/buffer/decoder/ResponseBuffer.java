package org.obd.metrics.buffer.decoder;

import org.obd.metrics.context.Service;

public interface ResponseBuffer extends Service {
	ResponseBuffer clear();

	ResponseBuffer addLast(Response command);

	Response get() throws InterruptedException;
	
	static ResponseBuffer instance() {
		return new DefaultResponseBuffer();
	}
}