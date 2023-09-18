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
package org.obd.metrics.api.model;

import java.util.HashSet;
import java.util.Set;

import org.obd.metrics.context.Context;
import org.obd.metrics.context.Service;

import lombok.extern.slf4j.Slf4j;

public interface Lifecycle {

	@Slf4j
	public static final class Subscription implements Lifecycle, Service {

		private final Set<Lifecycle> items = new HashSet<Lifecycle>();

		public void subscribe(Lifecycle lifecycle) {
			if (lifecycle == null) {
				log.debug("Specified lifecycle is null, skipping.");
			} else {
				if (this == lifecycle) {
					log.debug("We do not want to register itself.");
				} else {
					log.debug("Registering new lifecycle: '{}' listener", lifecycle.getClass().getSimpleName());
					items.add(lifecycle);
				}
			}
		}

		@Override
		public void onConnecting() {
			log.debug("Triggering event onConnecting");
			items.forEach(p -> {
				try {
					p.onConnecting();
				} catch (Exception e) {
					log.warn("Failed while executing onConnecting", e);
				}
			});
		}
		
		@Override
		public void onInternalError(String message, Throwable e) {
			log.debug("Triggering event onInternalError");
			items.forEach(p -> {
				try {
					p.onInternalError(message, e);
				} catch (Exception ex) {
					log.warn("Failed while executing onError", e);
				}
			});
		}

		
		
		@Override
		public void onError(String message, Throwable e) {
			log.debug("Triggering event onError");
			items.forEach(p -> {
				try {
					p.onError(message, e);
				} catch (Exception ex) {
					log.warn("Failed while executing onError", e);
				}
			});
		}

		@Override
		public void onRunning(VehicleCapabilities vehicleCapabilities) {
			log.debug("Triggering event onRunning");
			items.forEach(p -> {
				try {
					p.onRunning(vehicleCapabilities);
				} catch (Exception ex) {
					log.warn("Failed while executing onRunning", ex);
				}
			});
		}

		@Override
		public void onStopped() {
			log.debug("Triggering event onStopped");
			items.forEach(p -> {
				try {
					p.onStopped();
				} catch (Exception ex) {
					log.warn("Failed while executing onStopped", ex);
				}
			});
		}

		@Override
		public void onStopping() {
			log.debug("Triggering event onStopping");
			items.forEach(p -> {
				try {
					p.onStopping();
				} catch (Exception ex) {
					log.warn("Failed while executing onStopping", ex);
				}
			});
		}
	
		public static void notifyOnInternalError(String message) {
			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onInternalError(message, null);
			});
		}
		
		public static void notifyOnInternalError(String message, Throwable e) {
			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onInternalError(message, e);
			});
		}
	}
	
	
	default void onStopped() {
	}

	default void onStopping() {
	}

	default void onConnecting() {
	}

	default void onRunning(VehicleCapabilities vehicleCapabilities) {
	}

	default void onError(String message, Throwable e) {
	}
	
	default void onInternalError(String message, Throwable e) {
	}
}
