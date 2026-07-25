 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;

import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ModuleDiscoveryStatus;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.discovery.ModuleDiscoveryCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class ModuleDiscoveryResponseObserver<T> extends ReplyObserver<Reply<?>> {
	private static final String SUCCESS_CODE = "7E00";

	private final Subscription subscription;

	@Override
	public void onNext(Reply<?> reply) {
		try {
			final ModuleDiscoveryCommand command = (ModuleDiscoveryCommand) reply.getCommand();

			final String response = reply.getRaw().getMessage();
			log.info("Received module discovery response for header={}: {}", command.getHeader(), response);

			ModuleDiscoveryStatus status = ModuleDiscoveryStatus.ERROR;
			if (response.startsWith(SUCCESS_CODE)) {
				status = ModuleDiscoveryStatus.FOUND;
			} else if (reply.getRaw().isEmpty()) {
				status = ModuleDiscoveryStatus.NOT_FOUND;
			}

			log.info("Module discovery header={} status={}", command.getHeader(), status);
			subscription.onModuleDiscovered(command.getHeader(), status);

		} catch (Throwable e) {
			log.error("Failed to process module discovery response", e);
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(ModuleDiscoveryCommand.class);
	}
}
