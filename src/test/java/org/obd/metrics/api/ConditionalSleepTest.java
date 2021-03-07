package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalSleepTest {
	
	@Test
	void equalToSleepTimeCondition() throws InterruptedException {
		var conditionalSleep = ConditionalSleep
		        .builder()
		        .sleepTime(20l)
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

		var conditionalSleep = ConditionalSleep
		        .builder()
		        .sleepTime(20l)
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

		var conditionalSleep = ConditionalSleep
		        .builder()
		        .sleepTime(5l)
		        .condition(() -> true)
		        .build();

		int sleepTime = 170;
		long tt = System.currentTimeMillis();
		conditionalSleep.sleep(sleepTime);

		tt = System.currentTimeMillis() - tt;
		Assertions.assertThat(tt).isLessThan(50);
	}
}
