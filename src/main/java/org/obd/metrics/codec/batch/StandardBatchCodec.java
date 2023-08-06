package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

final class StandardBatchCodec extends Mode22BatchCodec {

	private static final int MODE_22_BATCH_SIZE = 3;

	StandardBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		super(BatchCodecType.STD, init, adjustments, query, commands, MODE_22_BATCH_SIZE);
	}
}
