package org.obd.metrics.codec.batch.mapper;

import java.util.List;
import java.util.Map;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

public interface BatchCommandsMapper {

	int getCacheHit(final String query);

	Map<ObdCommand, ConnectorResponse> convert(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse);

	static BatchCommandsMapper instance() {
		return new DefaultBatchCommandsMapper();
	}
}
