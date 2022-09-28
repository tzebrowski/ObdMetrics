package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

final class StandardBatchCodec extends AbstractBatchCodec {

	private static final int MODE_22_BATCH_SIZE = 3;
	private static final String MODE_22 = "22";

	StandardBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		super(BatchCodecType.STD, init, adjustments, query, commands);
	}

	@Override
	protected int determineBatchSize(final String mode) {
		return MODE_22.equals(mode) ? MODE_22_BATCH_SIZE : BATCH_SIZE;
	}
}
