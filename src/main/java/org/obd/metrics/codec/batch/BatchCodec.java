package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.raw.RawMessage;

public interface BatchCodec extends Codec<Map<ObdCommand, RawMessage>> {

	public static enum BatchCodecType {
		STN, STD
	}

	List<BatchObdCommand> encode();

	int getCacheHit(String query);

	static BatchCodec instance(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		return instance(BatchCodecType.STD, init, adjustments, query, commands);
	}

	static BatchCodec instance(final BatchCodecType codecType, final Init init, final Adjustments adjustments,
			final String query, final List<ObdCommand> commands) {

		switch (codecType) {
		case STN:
			return new STNBatchCodec(init, adjustments, query, commands);
		default:
			return new StandardBatchCodec(init, adjustments, query, commands);
		}
	}
}
