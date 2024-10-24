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
package org.obd.metrics.api.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;

import lombok.extern.slf4j.Slf4j;

//01 15 0B 0C 11 0D
//00D0:41155AFF0BFF1:0C000011000D00

//01 0C 10 0B 0D 05
//00B0:410C000010001:000B660D000000				

//01 0C 10 0B 0D
//00B0:410C000010001:000B660D000000

//0C 10 0B 0D 05 0F
//00F0:410C000010001:000B660D0005222:0F370000000000

// 01 05 0C
// 4105000c0000

// 01 0B 0C 0D 0E 0F 11
// 00e0:410bff0c00001:0d000e800f00112:00aaaaaaaaaaaa

// 01 13 15 1C 1F
// 00b0:411303155aff1:1c061f0000aaaa

// 01 01 03 04 05 06 07
// 0110:41010007e1001:030000040005002:0680078baaaaaa

//01 15 0B 0C 11 0D
//00D0:41155AFF0BFF1:0C000011000D00

@Slf4j
public class EcuAnswerGenerator {

	public MultiValuedMap<String, String> generate(Query query, int numberOfEntries) {
		final PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final MultiValuedMap<String, String> answers = new ArrayListValuedHashMap<String, String>();

		for (final ObdCommand command : build(pidRegistry, query)) {
			long ts = System.currentTimeMillis();

			final String queryStr = command.getQuery();
			log.info("Generating answers for the query: {}", queryStr);

			final String pattern = createPattern(queryStr, pidRegistry);
			log.debug("Created pattern: {} for the query: {}", pattern, queryStr);

			final String[] pids = queryStr.split(" ");

			if (pids.length == 2) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < 255; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					answers.put(queryStr, decorateWithAnswerCodeAndColon(String.format(pattern, i1)));
				}

			} else if (pids.length == 3) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;
				int m2 = pidRegistry.findBy(pids[2]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < numberOfEntries; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					int i2 = r1.nextInt(1, 255 * m2 + 1);
					answers.put(queryStr, decorateWithAnswerCodeAndColon(String.format(pattern, i1, i2)));
				}

			} else if (pids.length == 4) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;
				int m2 = pidRegistry.findBy(pids[2]).getLength() == 1 ? 1 : 255;
				int m3 = pidRegistry.findBy(pids[3]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < numberOfEntries; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					int i2 = r1.nextInt(1, 255 * m2 + 1);
					int i3 = r1.nextInt(1, 255 * m3 + 1);
					answers.put(queryStr, decorateWithAnswerCodeAndColon(String.format(pattern, i1, i2, i3)));
				}

			} else if (pids.length == 5) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;
				int m2 = pidRegistry.findBy(pids[2]).getLength() == 1 ? 1 : 255;
				int m3 = pidRegistry.findBy(pids[3]).getLength() == 1 ? 1 : 255;
				int m4 = pidRegistry.findBy(pids[4]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < numberOfEntries; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					int i2 = r1.nextInt(1, 255 * m2 + 1);
					int i3 = r1.nextInt(1, 255 * m3 + 1);
					int i4 = r1.nextInt(1, 255 * m4 + 1);
					answers.put(queryStr, decorateWithAnswerCodeAndColon(String.format(pattern, i1, i2, i3, i4)));
				}

			} else if (pids.length == 6) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;
				int m2 = pidRegistry.findBy(pids[2]).getLength() == 1 ? 1 : 255;
				int m3 = pidRegistry.findBy(pids[3]).getLength() == 1 ? 1 : 255;
				int m4 = pidRegistry.findBy(pids[4]).getLength() == 1 ? 1 : 255;
				int m5 = pidRegistry.findBy(pids[5]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < numberOfEntries; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					int i2 = r1.nextInt(1, 255 * m2 + 1);
					int i3 = r1.nextInt(1, 255 * m3 + 1);
					int i4 = r1.nextInt(1, 255 * m4 + 1);
					int i5 = r1.nextInt(1, 255 * m5 + 1);
					answers.put(queryStr, decorateWithAnswerCodeAndColon(String.format(pattern, i1, i2, i3, i4, i5)));
				}

			} else if (pids.length == 7) {
				final ThreadLocalRandom r1 = ThreadLocalRandom.current();

				int m1 = pidRegistry.findBy(pids[1]).getLength() == 1 ? 1 : 255;
				int m2 = pidRegistry.findBy(pids[2]).getLength() == 1 ? 1 : 255;
				int m3 = pidRegistry.findBy(pids[3]).getLength() == 1 ? 1 : 255;
				int m4 = pidRegistry.findBy(pids[4]).getLength() == 1 ? 1 : 255;
				int m5 = pidRegistry.findBy(pids[5]).getLength() == 1 ? 1 : 255;
				int m6 = pidRegistry.findBy(pids[6]).getLength() == 1 ? 1 : 255;

				for (int i = 0; i < numberOfEntries; i++) {
					int i1 = r1.nextInt(1, 255 * m1 + 1);
					int i2 = r1.nextInt(1, 255 * m2 + 1);
					int i3 = r1.nextInt(1, 255 * m3 + 1);
					int i4 = r1.nextInt(1, 255 * m4 + 1);
					int i5 = r1.nextInt(1, 255 * m5 + 1);
					int i6 = r1.nextInt(1, 255 * m6 + 1);

					answers.put(queryStr,
							decorateWithAnswerCodeAndColon(String.format(pattern, i1, i2, i3, i4, i5, i6)));
				}
			}
			ts = System.currentTimeMillis() - ts;
			log.info("Generated: {} unique entries for the query: {}. It took {}ms", answers.get(queryStr).size(),
					queryStr, ts);

		}

		return answers;
	}

	private String createPattern(String query, PIDsRegistry pidRegistry) {
		final String[] pids = query.split(" ");
		String pattern = "";
		for (int k = 1; k < pids.length; k++) {
			String id = pids[k];
			final PidDefinition pid = pidRegistry.findBy(id);
			pattern += id;
			if (pid.getLength() == 1) {
				pattern += "%02X";
			} else if (pid.getLength() == 2) {
				pattern += "%04X";
			}
		}
		return pattern;
	}

	private String decorateWithAnswerCodeAndColon(String in) {
		String out = "41";
		if (in.length() <= 10) {
			out += in;
		} else {
			String first = in.substring(0, 10);
			out += first + "1:";
			if (in.length() > 24) {
				String second = in.substring(10, 24);
				out += second + "2:";
				String third = in.substring(24, in.length());
				out += third;
				out = "00F0:" + out;
			} else {
				String second = in.substring(10, in.length());
				out += second;
				out = "00F0:" + out;
			}
		}
		return out;
	}

	private List<ObdCommand> build(PidDefinitionRegistry pidRegistry, Query query) {
		final List<ObdCommand> commands = query.getPids().stream().map(idToPid(pidRegistry)).filter(Objects::nonNull)
				.sorted((c1, c2) -> c2.getPid().compareTo(c1.getPid())).collect(Collectors.toList());
		final List<ObdCommand> result = new ArrayList<>();
		// collect first commands that support batch fetching
		final List<ObdCommand> obdCommands = commands.stream()
				.filter(p -> CommandType.OBD.equals(p.getPid().getCommandType()))
				.filter(distinctByKey(c -> c.getPid().getPid())).collect(Collectors.toList());

		List<BatchObdCommand> encode = BatchCodec.builder().commands(obdCommands).build().encode();

		result.addAll(encode);
		// add at the end commands that does not support batch fetching
		result.addAll(commands.stream().filter(p -> !CommandType.OBD.equals(p.getPid().getCommandType()))
				.collect(Collectors.toList()));

		log.info("Build target commands list: {}", result);
		return result;
	}

	private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private Function<? super Long, ? extends ObdCommand> idToPid(PidDefinitionRegistry pidRegistry) {
		return pid -> {
			final PidDefinition findBy = pidRegistry.findBy(pid);
			return findBy == null ? null : new ObdCommand(findBy);
		};
	}
}
