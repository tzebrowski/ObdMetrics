package org.obd.metrics;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinitionRegistry.PidDefinitionRegistryBuilder;
import org.obd.metrics.pid.Resource;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;


public class PIDsRegistryCache {

	@RequiredArgsConstructor
	static class PIDsRegistryProxy implements PIDsRegistry {
		@Delegate
		final PidDefinitionRegistry registry;
	}
	
	static final Map<String, PIDsRegistry> cache = new HashedMap<>();

	public static PIDsRegistry get(final String... sources) {
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
			
			final PIDsRegistryProxy proxy = new PIDsRegistryProxy(builder.build());
			cache.put(key, proxy);
			return proxy;
		}
	}
}