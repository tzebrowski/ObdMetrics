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
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.routine.RoutineCommand;
import org.obd.metrics.command.routine.RoutineExecutionStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class RoutinesResponseObserver<T> extends ReplyObserver<Reply<?>> {
	private final Subscription subscription;
	
	@Override
	public void onNext(Reply<?> reply) {
		try {
			final RoutineCommand routine = (RoutineCommand) reply.getCommand();
			
			final String response = reply.getRaw().getMessage();
			log.info("Received routine response {}={} ", routine, response);

			
			final String successCode = getSuccessCode(routine);
	
			RoutineExecutionStatus status = RoutineExecutionStatus.ERROR;
			if (response.startsWith(successCode)) {
				status = RoutineExecutionStatus.SUCCESS;
			} else if (reply.getRaw().isEmpty()) {
				status = RoutineExecutionStatus.NO_DATA;
			}

			log.info("Routine  {} status={}, predicted success-code: {}", routine.getQuery(), status, successCode);
			subscription.onRoutineCompleted(routine, status);

		} catch (Throwable e) {
			log.error("Failed to process roiutine response", e);
		}
	}

	private String getSuccessCode(final RoutineCommand routine) {
		final String mode = routine.getPid().getMode();
		if (mode == null || mode.isEmpty()) {
			return "";
		}

		final int firstHexValue = Character.digit(mode.charAt(0), 16);
		final int code = 4 + firstHexValue;
		return String.format("%X%s", code, mode.substring(1)).toUpperCase();
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(RoutineCommand.class);
	}
}
