package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class SparkAdvanceTest  implements PidTest{
	@Test
	public void possitiveTest() throws IOException {
		mode22Test("62181204", 2.0);
	}
}
