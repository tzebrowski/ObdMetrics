package org.obd.metrics.codec.batch.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 *  Naive cache implementation
 */
final class BatchMessageMappingsCache {

	private final Map<String, BatchMessageMapping> mappings = new HashMap<>();

	BatchMessageMapping lookup(String query) {
		final BatchMessageMapping mapping = mappings.get(query);
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
