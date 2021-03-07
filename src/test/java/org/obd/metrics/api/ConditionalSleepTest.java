package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalSleepTest {
	
	@Test
	void sleepTest() throws InterruptedException {
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
	void sleep2Test() throws InterruptedException {

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
}
