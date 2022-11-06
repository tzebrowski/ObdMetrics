package org.obd.metrics.codec.batch;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

final class BatchResponseMappingCache {
	private final Map<String, BatchResponseMapping> mappings = new HashMap<>();

	Map<ObdCommand, ConnectorResponse> lookup(String query, ConnectorResponse connectorResponse) {
		final Map<ObdCommand, ConnectorResponse> values = new HashMap<>();
		final BatchResponseMapping mapping = mappings.get(query);

		mapping.updateCacheHit();

		mapping.getMappings().forEach(it -> {
			values.put(it.getCommand(), new BatchMessage(it, connectorResponse));
		});

		return values;
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
