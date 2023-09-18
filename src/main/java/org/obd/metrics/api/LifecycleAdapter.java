/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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
