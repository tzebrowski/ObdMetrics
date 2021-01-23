package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class TargetAirMassFlowTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("6218670059", 8.9);
	}
}
