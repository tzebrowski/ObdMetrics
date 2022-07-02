package org.obd.metrics.api;

import org.obd.metrics.api.model.DeviceProperties;
import org.obd.metrics.api.model.Lifecycle;

import lombok.Delegate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class SimpleLifecycle implements Lifecycle {

	@Getter
	boolean errorOccurred;

	@Getter
	String message;
	
	@Delegate
	private DeviceProperties properties;

	@Override
	public void onRunning(DeviceProperties props) {
		log.info("Device properties {}", props.getProperties());
		this.properties = props;
	}
	
	@Override
	public void onError(String message, Throwable e) {
		errorOccurred = true;
		this.message = message;
	}
	
	void reset() {
		message = null;
		errorOccurred = false;
	}
}