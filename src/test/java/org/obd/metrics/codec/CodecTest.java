package org.obd.metrics.codec;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.assertj.core.api.Assertions;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry.PidDefinitionRegistryBuilder;
import org.obd.metrics.raw.RawMessage;

public interface CodecTest {

	
	public static class PidRegistryCache {
		static final Map<String, PidDefinitionRegistry> cache = new HashedMap<>();

		public static PidDefinitionRegistry get(final String... sources) {
			final String key = Stream.of(sources)
			        .map(Object::toString)
			        .collect(Collectors.joining(", "));

			if (cache.containsKey(key)) {
				return cache.get(key);
			} else {
				PidDefinitionRegistryBuilder builder = PidDefinitionRegistry.builder();
				for (String s : sources) {
					final InputStream source = Thread.currentThread().getContextClassLoader()
					        .getResourceAsStream(s);
					builder = builder.source(source);
				}
				final PidDefinitionRegistry pidRegistry = builder.build();
				cache.put(key, pidRegistry);
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
		final Codec<?> codec = codecRegistry.findCodec(pidDef);

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Object actualValue = codec.decode(pidDef, RawMessage.wrap(rawData.getBytes()));
			Assertions.assertThat(actualValue).isEqualTo(expectedValue);
		}
	}
}
