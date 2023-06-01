package org.obd.metrics.codec.batch.mapper;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Naive cache implementation
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MappingsCache {

	private final Map<String, BatchMessageMapping> mappings = new HashMap<>();

	BatchMessageMapping lookup(String query) {
		final BatchMessageMapping mapping = mappings.get(query);
		if (mapping == null) {
			log.error("no mapping found for {}", query);
			return null;
		}
		mapping.updateCacheHit();
		return mapping;
	}

	boolean contains(String query) {
		return mappings.containsKey(query);
	}

	void insert(String query, BatchMessageMapping mapping) {
		mappings.put(query, mapping);
	}

	int getCacheHit(final String query) {
		return mappings.get(query).getHit();
	}
}
