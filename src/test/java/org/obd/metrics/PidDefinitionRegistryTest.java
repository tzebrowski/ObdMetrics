package org.obd.metrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;
import org.obd.metrics.pid.ValueType;

public class PidDefinitionRegistryTest {

	@Test
	public void registerCollectionOfPids() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
					Resource.builder().inputStream(source).name("mode01.json").build()).build();

			PidDefinition pidDefinition = new PidDefinition(10001l, 2, "A+B", "01", "CC", "C", "dummy pid", 0, 100,
			        ValueType.DOUBLE);

			pidRegistry.register(java.util.Arrays.asList(pidDefinition));

			PidDefinition findBy = pidRegistry.findBy("CC");
			Assertions.assertThat(findBy).isNotNull();
			Assertions.assertThat(findBy.getId()).isEqualTo(10001l);
			Assertions.assertThat(findBy.getFormula()).isEqualTo("A+B");

		}
	}

	@Test
	public void registerNullPid() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
					Resource.builder().inputStream(source).name("mode01.json").build()).build();
			org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
				pidRegistry.register((PidDefinition) null);
			});
		}
	}

	@Test
	public void registerPid() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
					Resource.builder().inputStream(source).name("mode01.json").build()).build();

			PidDefinition def = new PidDefinition(1000l, 2, "A+B", "01", "FF", "C", "dummy pid", 0, 100,
			        ValueType.DOUBLE);

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

			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
					Resource.builder().inputStream(source).name("mode01.json").build()).build();

			PidDefinition findBy = pidRegistry.findBy("0C");
			Assertions.assertThat(findBy).isNotNull();
		}
	}

	@Test
	public void findByNull() {
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(null).build();
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
			pidRegistry.findBy((Long) null);
		});
	}
	
	
	@Test
	public void findAllBy() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		PidDefinition findBy = pidRegistry.findBy("15");
		
		final Collection<PidDefinition> o2 = pidRegistry.findAllBy(findBy);
		
		Assertions.assertThat(o2).hasSize(2);
		Iterator<PidDefinition> iterator = o2.iterator();
		
		PidDefinition n1 = iterator.next();
		PidDefinition n2 = iterator.next();
		
		Assertions.assertThat(n1.getId()).isEqualTo(22l);
		Assertions.assertThat(n2.getId()).isEqualTo(23l);
	}
}
