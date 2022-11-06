package org.obd.metrics.codec.batch.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 *  Naive cache implementation
 */
public final class BatchResponseMappingsCache {

	private final Map<String, BatchResponseMapping> mappings = new HashMap<>();

	public BatchResponseMapping lookup(String query) {
		final BatchResponseMapping mapping = mappings.get(query);
		mapping.updateCacheHit();
		return mapping;
	}

	public boolean contains(String query) {
		return mappings.containsKey(query);
	}

	public void insert(String query, BatchResponseMapping mapping) {
		mappings.put(query, mapping);
	}

	public int getCacheHit(final String query) {
		return mappings.get(query).getHit();
	}
}
