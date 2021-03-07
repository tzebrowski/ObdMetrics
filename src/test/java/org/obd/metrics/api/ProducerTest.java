package org.obd.metrics.api;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ProducerPolicy;

//TODO - rework
public class ProducerTest {

	@Test
	void sleepTest() {

		long timeout = Producer.waitTime;
		long cnt = new Producer(new CommandsBuffer(), ProducerPolicy.DEFAULT, Arrays.asList()).sleep(timeout);
		Assertions.assertThat(cnt).isEqualTo(timeout);
	}

	@Test
	void sleep2Test() {

		long timeout = 50;
		long cnt = new Producer(new CommandsBuffer(), ProducerPolicy.DEFAULT, Arrays.asList()).sleep(timeout);
		// Assertions.assertThat(cnt).
	}
}
