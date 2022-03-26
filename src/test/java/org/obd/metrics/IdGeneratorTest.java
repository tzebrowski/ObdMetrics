package org.obd.metrics;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.raw.IdGenerator;

public class IdGeneratorTest {

	@Test
	public void generateId_1Test() {
		byte[] bytes = "FFFFFF".getBytes();

		long c = IdGenerator.generateId(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170000770L);

		c = IdGenerator.generateId(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170077770);

		c = IdGenerator.generateId(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(177777770);
		
	}

	@Test
	public void generateId_2Test() {
		byte[] bytes = "00FFDC".getBytes();

		long c = IdGenerator.generateId(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170000528);

		c = IdGenerator.generateId(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170053570);

		c = IdGenerator.generateId(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(175357747);
		
	}

	
	@Test
	public void cacheTest() {
		byte[] bytes = "00FFDC".getBytes();
		
		int aa = 0;
		for (int i = 0; i < 10000; i++) {

			long ss = System.nanoTime();
			

			IdGenerator.generateId(1, 17L, 0, bytes);

			IdGenerator.generateId(2, 17L, 0, bytes);

			IdGenerator.generateId(3, 17L, 0, bytes);
			ss = System.nanoTime() - ss;
			if (ss <= 300) {
				aa = i;
				break;

			}
		}

		Assertions.assertThat(aa).isLessThan(3000);
	}
}
