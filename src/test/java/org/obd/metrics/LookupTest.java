package org.obd.metrics;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidRegistry;

public class LookupTest {

	@Test
	void t0() {
		

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");
		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
		
		List<ObdMetric> metrics = new ArrayList<>();
		metrics.add(ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(6l))).raw("410522").value(-6).build());
		
		metrics.add(ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(16l))).raw("410f2f").value(7).build());
	
		
		ObdMetric coolant = ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(6l))).raw("410517").value(-17).build();
		
		Assertions.assertThat(metrics.indexOf(coolant)).isEqualTo(0);
		
	}
}
