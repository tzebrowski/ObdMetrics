package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

final class StandardBatchCodec extends AbstractBatchCodec {

	private static final int MODE_22_BATCH_SIZE = 3;
	private final Integer mode22BatchSize;

	StandardBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		super(BatchCodecType.STD, init, adjustments, query, commands);
		this.mode22BatchSize = adjustments.getMode22BatchSize();
	}

	@Override
	protected int determineBatchSize(final String mode) {
		if (MODE_22.equals(mode)) {
			return mode22BatchSize == null ? MODE_22_BATCH_SIZE : mode22BatchSize;
		} else {
			return DEFAULT_BATCH_SIZE;
		}
	}
}
