package org.obd.metrics.codec;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.assertj.core.api.Assertions;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public interface CodecTest {

	public static class PidRegistryCache {
		static final Map<String, PidDefinitionRegistry> cache = new HashedMap<>();

		public  static PidDefinitionRegistry get(String pidSource) {
			if (cache.containsKey(pidSource)) {
				return cache.get(pidSource);
			} else {

				final InputStream source = Thread.currentThread().getContextClassLoader()
				        .getResourceAsStream(pidSource);
				final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();
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
		final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidDef));

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Object actualValue = codec.decode(pidDef, RawMessage.instance(rawData.getBytes()));
			Assertions.assertThat(actualValue).isEqualTo(expectedValue);
		}
	}
}
