package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

//TODO - rework
public class ConditionalSleepTest {

	@Test
	void sleepTest() {
		var conditionalSleep = ConditionalSleep
		        .builder()
		        .waitTime(20l)
		        .condition(() -> false)
		        .build();
		
		long cnt = conditionalSleep.sleep(20);
		Assertions.assertThat(cnt).isEqualTo(20);
	}

	@Test
	void sleep2Test() {

		var conditionalSleep = ConditionalSleep
		        .builder()
		        .waitTime(20l)
		        .condition(() -> false)
		        .build();
		
		int sleepTime = 50;
		long cnt = conditionalSleep.sleep(sleepTime);
		Assertions.assertThat(cnt).isGreaterThan(sleepTime);
	}
}
