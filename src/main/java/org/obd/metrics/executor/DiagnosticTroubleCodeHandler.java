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
package org.obd.metrics.executor;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class DiagnosticTroubleCodeHandler implements CommandHandler {
	private static final int DELAY_MS = 100;
	private static final int MAX_PULL_ATTEMPTS = 15;
	private final DiagnosticTroubleCodeReader diagnosticTroubleCodeReader = new DiagnosticTroubleCodeReader();
	private final DiagnosticTroubleCodeCleaner diagnosticTroubleCodeCleaner = new DiagnosticTroubleCodeCleaner();

	DiagnosticTroubleCodeHandler() {

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(diagnosticTroubleCodeReader);
			p.subscribe(diagnosticTroubleCodeCleaner);
		});
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		log.info("Executing DiagnosticTroubleCodeHandler asynchronously");

		CompletableFuture.runAsync(() -> {
			Set<DiagnosticTroubleCode> dtcValue = null;

			for (int i = 0; i < MAX_PULL_ATTEMPTS; i++) {
				dtcValue = diagnosticTroubleCodeReader.getValue();
				if (dtcValue != null) {
					break;
				}

				try {
					Thread.sleep(DELAY_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("DTC polling thread was interrupted", e);
					break;
				}
			}

			log.info("Found Diagnostic Trouble Codee, length: {}.", dtcValue.size());
			log.info("Status of the Diagnostic Trouble Codes cleanup: {}.", diagnosticTroubleCodeCleaner.getValue());

			final Set<DiagnosticTroubleCode> finalDtcValue = dtcValue;
			final DiagnosticTroubleCodeClearStatus finalCleanerValue = diagnosticTroubleCodeCleaner.getValue();

			Context.apply(ctx -> {
				ctx.resolve(Subscription.class).apply(p -> {
					p.onDTCCompleted(finalDtcValue, finalCleanerValue);
					diagnosticTroubleCodeReader.reset();
				});
			});
		});

		return CommandExecutionStatus.OK;
	}
}
