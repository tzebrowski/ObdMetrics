package org.obd.metrics;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry.PidDefinitionRegistryBuilder;

public class PidRegistryCache {
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