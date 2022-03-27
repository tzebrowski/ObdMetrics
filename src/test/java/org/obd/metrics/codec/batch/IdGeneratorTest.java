package org.obd.metrics.codec.batch;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.batch.IdGenerator;

public class IdGeneratorTest {

	@Test
	public void generateId_1Test() {
		byte[] bytes = "FFFFFFFF".getBytes();

		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(1777770);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(177777770);

		c = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(17777777770L);

	}

	@Test
	public void generateId_2Test() {
		byte[] bytes = "00FFDC".getBytes();

		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170528);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(1753570);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(175357747);

	}

	@Test
	public void cacheTest() {
		byte[] bytes = "00FFDC".getBytes();

		int iterationHit = 0;
		for (int counter = 0; counter < 100000; counter++) {

			long executionTime = System.nanoTime();
			IdGenerator.generate(1, 17L, 0, bytes);

			IdGenerator.generate(2, 17L, 0, bytes);

			IdGenerator.generate(3, 17L, 0, bytes);
			executionTime = System.nanoTime() - executionTime;
			if (executionTime <= 200) {
				iterationHit = counter;
				break;

			}
		}
		Assertions.assertThat(iterationHit).isBetween(1, 1000);
	}
}
