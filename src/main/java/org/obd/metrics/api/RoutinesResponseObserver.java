/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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

import java.util.Arrays;
import java.util.List;

import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.routine.RoutineCommand;
import org.obd.metrics.command.routine.RoutineExecutionStatus;
import org.obd.metrics.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class RoutinesResponseObserver<T> extends ReplyObserver<Reply<?>> {

	@Override
	public void onNext(Reply<?> reply) {
		try {
			final RoutineCommand routine = (RoutineCommand) reply.getCommand();
			final String successCode = getSuccessCode(routine);
			
			final String response = reply.getRaw().getMessage();
			log.info("Received routine response {}={}, predicted success-code: {}", routine, response,
					successCode);

			Context.apply(ctx -> {
				ctx.resolve(Subscription.class).apply(p -> {
					ctx.resolve(EventsPublishlisher.class).apply(e -> {

						RoutineExecutionStatus status = RoutineExecutionStatus.ERROR;

						if (response.startsWith(successCode)) {
							status = RoutineExecutionStatus.SUCCESS;
						} else if (reply.getRaw().isEmpty()) {
							status = RoutineExecutionStatus.NO_DATA;
						}

						log.info("Routine  {} status={}, predicted success-code: {}", routine.getQuery(), status, successCode);
						p.onRoutineCompleted(routine, status);
					});
				});
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private String getSuccessCode(final RoutineCommand routine) {
		final int code = 4 + Integer.parseInt("" + routine.getPid().getMode().charAt(0));
		return String.format("%d%s",code,routine.getPid().getMode().charAt(1))
				.toUpperCase();
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(RoutineCommand.class);
	}
}
