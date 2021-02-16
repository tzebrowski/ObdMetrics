package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class SparkAdvanceTest  implements Mode22Test{
	@Test
	public void possitiveTest() throws IOException {
		assertThat("62181204", 2.0);
	}
}
