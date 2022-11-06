package org.obd.metrics.codec.batch;

import java.util.HashMap;
import java.util.Map;

/**
 *  Naive cache implementation
 */
final class BatchResponseMappingsCache {

	private final Map<String, BatchResponseMapping> mappings = new HashMap<>();

	BatchResponseMapping lookup(String query) {
		final BatchResponseMapping mapping = mappings.get(query);
		mapping.updateCacheHit();
		return mapping;
	}

	boolean contains(String query) {
		return mappings.containsKey(query);
	}

	void insert(String query, BatchResponseMapping mapping) {
		mappings.put(query, mapping);
	}

	int getCacheHit(final String query) {
		return mappings.get(query).getHit();
	}

}
