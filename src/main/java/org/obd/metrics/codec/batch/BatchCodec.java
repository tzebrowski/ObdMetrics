package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Builder;

public interface BatchCodec extends Codec<Map<ObdCommand, ConnectorResponse>> {

	List<BatchObdCommand> encode();

	int getCacheHit(String query);

	@Builder
	static BatchCodec instance(BatchCodecType codecType, Init init, Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {

		if (init == null) {
			init = Init.DEFAULT;
		}

		if (adjustments == null) {
			adjustments = Adjustments.DEFAULT;
		}

		if (codecType == null) {
			codecType = BatchCodecType.STD;

			if (adjustments.getStNxx().isEnabled()) {
				codecType = BatchCodecType.STNxx;
			}
		}

		switch (codecType) {
		case STNxx:
			return new STNxxBatchCodec(init, adjustments, query, commands);
		default:
			return new StandardBatchCodec(init, adjustments, query, commands);
		}
	}
}
