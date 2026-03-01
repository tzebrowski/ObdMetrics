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
package org.obd.metrics.codec.batch.decoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class DefaultBatchMessageDecoder implements BatchMessageDecoder {

	private static final String[] DELIMETERS = new String[] { "0:", "1:", "2:", "3:", "4:", "5:", "6:", "7:", "8:",
			"9:", "A:", "B:", "C:", "D:", "E:", "F:" };

	private final MappingsCache cache = new MappingsCache();
	private final BatchPolicy batchPolicy;

	@Override
	public Map<ObdCommand, ConnectorResponse> decode(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {
		final int[] colons = connectorResponse.getColonPositions();

		Map<ObdCommand, ConnectorResponse> mapping = cache.lookup(query, colons);

		if (mapping == null) {
			mapping = new HashMap<ObdCommand, ConnectorResponse>();

			final List<PIDPositionTemplate> template = createTemplateFor(query, commands, connectorResponse);

			if (template == null) {
				log.error("No template created for: '{}'", connectorResponse.getMessage());
			} else {

				for (final PIDPositionTemplate pidPositionTemplate : template) {
					mapping.put(pidPositionTemplate.getCommand(),
							new BatchConnectorResponse(pidPositionTemplate, connectorResponse));
				}

				cache.insert(query, colons, mapping);
			}
		} else {
			for (final ConnectorResponse response : mapping.values()) {
				((BatchConnectorResponse) response).updateBuffer(connectorResponse);
			}
		}

		return mapping;
	}

	private List<PIDPositionTemplate> createTemplateFor(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {

		final String predictedAnswerCode = commands.iterator().next().getPid().getPredictedSuccessCode();

		final int colonFirstIndexOf = connectorResponse.getColonPositions()[0];
		final int codeIndexOf = connectorResponse.indexOf(predictedAnswerCode.getBytes(), predictedAnswerCode.length(),
				colonFirstIndexOf > 0 ? colonFirstIndexOf : 0);

		if (codeIndexOf == 0 || codeIndexOf == 3 || codeIndexOf == 5
				|| (colonFirstIndexOf > 0 && (codeIndexOf - colonFirstIndexOf) == 1)) {

			final List<PIDPositionTemplate> result = new ArrayList<PIDPositionTemplate>();

			int start = codeIndexOf;

			for (final ObdCommand command : commands) {

				final PidDefinition pidDefinition = command.getPid();

				String pidId = pidDefinition.getPid();
				int pidLength = pidId.length();
				int pidIdIndexOf = connectorResponse.indexOf(pidId.getBytes(), pidLength, start);

				if (log.isDebugEnabled()) {
					log.debug("Found pid={}, indexOf={} for message={}, query={}", pidId, pidIdIndexOf,
							connectorResponse.getMessage(), query);
				}

				if (pidIdIndexOf == -1) {
					int length = pidLength;
					String id = pidId;
					for (final String delim : DELIMETERS) {
						pidLength = length;
						pidId = id;

						if (pidLength == ConnectorResponse.TWO_TOKENS_LENGTH) {
							pidId = pidId.substring(0, ConnectorResponse.TOKEN_LENGTH) + delim + pidId
									.substring(ConnectorResponse.TOKEN_LENGTH, ConnectorResponse.TWO_TOKENS_LENGTH);
							pidLength = pidId.length();
							pidIdIndexOf = connectorResponse.indexOf(pidId.getBytes(), pidLength, start);

							if (log.isDebugEnabled()) {
								log.debug("Another iteration. Found pid={}, indexOf={}", pidId, pidIdIndexOf);
							}
						}
						if (pidIdIndexOf == -1) {
							continue;
						} else {
							break;
						}
					}
				}

				if (pidIdIndexOf == -1) {
					start = 0;
					pidId = pidDefinition.getPid();
					pidLength = pidId.length();
					pidIdIndexOf = connectorResponse.indexOf(pidId.getBytes(), pidLength, start);

					if (pidIdIndexOf == -1) {
						log.error("Did not found mapping for: {}", pidId);
						log.error("Mapping for id={}, indexOf={}, pidLength={}", pidId, pidIdIndexOf, pidIdIndexOf);
						continue;
					}
				}

				start = pidIdIndexOf + pidLength;

				if (connectorResponse.at(start) == ConnectorResponse.COLON
						|| connectorResponse.at(start + 1) == ConnectorResponse.COLON) {
					start += ConnectorResponse.TOKEN_LENGTH;
				}

				int end = start + (pidDefinition.getLength() * ConnectorResponse.TOKEN_LENGTH);

				if (connectorResponse.at(end - 1) == ConnectorResponse.COLON) {
					end += ConnectorResponse.TOKEN_LENGTH;
				} else {
					//
					for (int pos = start; pos < start
							+ (pidDefinition.getLength() * ConnectorResponse.TOKEN_LENGTH); pos++) {
						if (connectorResponse.at(pos) == ConnectorResponse.COLON) {
							end += ConnectorResponse.TOKEN_LENGTH;
						}
					}
				}

				final PIDPositionTemplate positionTemplate = new PIDPositionTemplate(command, start, end);
				log.info("Built template: {}", positionTemplate);
				result.add(positionTemplate);
				continue;
			}
			if (batchPolicy.isStrictValidationEnabled() && result.size() != commands.size()) {
				log.error("Did not find all PIDs within given message template. " + "Found={}, expected={}",
						result.size(), commands.size());
			} else {
				return result;
			}
		} else {
			log.warn(
					"Answer code for query: '{}' was not correct: {}. Predicated answer code: {}. "
							+ "Predicted code index: {}, First colon index: {}. Colons: {}",
					query, connectorResponse.getMessage(), predictedAnswerCode, codeIndexOf, colonFirstIndexOf,
					Arrays.toString(connectorResponse.getColonPositions()));
		}
		return null;
	}
}
