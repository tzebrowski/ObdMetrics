package org.obd.metrics;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public class PidsRegistryTest {

	@Test
	public void registerCollectionOfPids() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			
			PidDefinition def = new PidDefinition(10001l, 2, "A+B", "01", "CC", "C", "dummy pid",0, 100,PidDefinition.Type.DOUBLE);
			
			pidRegistry.register(java.util.Arrays.asList(def));
			
			PidDefinition findBy = pidRegistry.findBy("CC");
			Assertions.assertThat(findBy).isNotNull();
			Assertions.assertThat(findBy.getId()).isEqualTo(10001l);
			Assertions.assertThat(findBy.getFormula()).isEqualTo("A+B");
			
		}
	}
	
	@Test
	public void registerPid() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			
			PidDefinition def = new PidDefinition(1000l, 2, "A+B", "01", "FF", "C", "dummy pid",0, 100,PidDefinition.Type.DOUBLE);
			
			pidRegistry.register(def);
			
			PidDefinition findBy = pidRegistry.findBy("FF");
			Assertions.assertThat(findBy).isNotNull();
			Assertions.assertThat(findBy.getId()).isEqualTo(1000l);
			Assertions.assertThat(findBy.getFormula()).isEqualTo("A+B");
			
		}
	}
	
	
	@Test
	public void findByModeAndPid() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			
			PidDefinition findBy = pidRegistry.findBy("0c");
			Assertions.assertThat(findBy).isNotNull();
		}
	}
}
