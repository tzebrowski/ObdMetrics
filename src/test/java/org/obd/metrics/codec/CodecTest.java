package org.obd.metrics.codec;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.assertj.core.api.Assertions;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public interface CodecTest {

	static class PidRegistryCache {
		static final Map<String, PidRegistry> cache = new HashedMap<>();

		static PidRegistry get(String pidSource) {
			if (cache.containsKey(pidSource)) {
				return cache.get(pidSource);
			} else {

				final InputStream source = Thread.currentThread().getContextClassLoader()
				        .getResourceAsStream(pidSource);
				final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
				cache.put(pidSource, pidRegistry);
				return pidRegistry;
			}
		}
	}

	default void assertEquals(String pid, String pidSource, String rawData, Object expectedValue) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();
		final PidDefinition pidDef = PidRegistryCache.get(pidSource).findBy(pid);
		Assertions.assertThat(pidDef).isNotNull();
		final Optional<Codec<?>> codec = codecRegistry.findCodec(new ObdCommand(pidDef));

		if (codec.isPresent()) {
			final Object actualValue = codec.get().decode(pidDef, rawData);
			Assertions.assertThat(actualValue).isEqualTo(expectedValue);
		} else {
			Assertions.fail("No codec available for PID: {}", pid);
		}
	}
}
