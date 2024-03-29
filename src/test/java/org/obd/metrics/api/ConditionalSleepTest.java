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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalSleepTest {

	@Test
	void equalToSleepTimeCondition() throws InterruptedException {
		ConditionalSleep conditionalSleep = ConditionalSleep
		        .builder()
		        .slice(20l)
		        .condition(() -> false)
		        .build();
		long tt = System.currentTimeMillis();
		long sleepTime = 20;
		conditionalSleep.sleep(sleepTime);
		tt = System.currentTimeMillis() - tt;
		Assertions.assertThat(tt).isGreaterThanOrEqualTo(sleepTime);
	}

	@Test
	void greaterThanSleepTimeCondition() throws InterruptedException {

		ConditionalSleep conditionalSleep = ConditionalSleep
		        .builder()
		        .slice(20l)
		        .condition(() -> false)
		        .build();

		int sleepTime = 170;
		long tt = System.currentTimeMillis();
		conditionalSleep.sleep(sleepTime);

		tt = System.currentTimeMillis() - tt;
		Assertions.assertThat(tt).isGreaterThanOrEqualTo(sleepTime);
	}

	@Test
	void conditionTest() throws InterruptedException {

		ConditionalSleep conditionalSleep = ConditionalSleep
		        .builder()
		        .slice(5l)
		        .condition(() -> true)
		        .build();

		int sleepTime = 170;
		long tt = System.currentTimeMillis();
		conditionalSleep.sleep(sleepTime);

		tt = System.currentTimeMillis() - tt;
		Assertions.assertThat(tt).isLessThan(50);
	}
}
