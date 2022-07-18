package org.obd.metrics;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry.PidDefinitionRegistryBuilder;
import org.obd.metrics.pid.Resource;

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
			for (final String source : sources) {
				final InputStream inputStream = Thread.currentThread().getContextClassLoader()
				        .getResourceAsStream(source);
				builder = builder.source(Resource.builder().inputStream(inputStream).name(source).build());
			}
			final PidDefinitionRegistry pidRegistry = builder.build();
			cache.put(key, pidRegistry);
			return pidRegistry;
		}
	}
}