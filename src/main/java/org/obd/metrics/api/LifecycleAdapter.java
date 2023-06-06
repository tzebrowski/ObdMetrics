package org.obd.metrics.api;

import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.VehicleCapabilities;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class LifecycleAdapter implements Lifecycle {
	
	protected volatile boolean isStopped = false;
	protected volatile boolean isRunning = false;
	
	@Override
	public void onRunning(VehicleCapabilities vehicleCapabilities) {
		log.info("Received onRunning event. Starting {} thread.", getClass().getSimpleName());
		isRunning = true;
	}

	@Override
	public void onStopping() {
		log.info("Received onStopping event. Stopping {} thread.", getClass().getSimpleName());
		isStopped = true;
	}
}
