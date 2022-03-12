package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;

public interface BatchCodec extends Codec<Map<ObdCommand,RawMessage>> {
	
	List<BatchObdCommand> encode();
	int getCacheHit(String query);

	static BatchCodec instance(String query, List<ObdCommand> commands) {
		return new DefaultBatchCodec(query, commands);
	}
}
