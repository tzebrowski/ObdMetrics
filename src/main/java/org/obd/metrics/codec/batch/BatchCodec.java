package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.raw.RawMessage;

public interface BatchCodec extends Codec<Map<ObdCommand, RawMessage>> {

	List<BatchObdCommand> encode();

	int getCacheHit(String query);

	static BatchCodec instance(final Adjustments adjustments, final String query, final List<ObdCommand> commands) {
		return new DefaultBatchCodec(adjustments, query, commands);
	}
}
