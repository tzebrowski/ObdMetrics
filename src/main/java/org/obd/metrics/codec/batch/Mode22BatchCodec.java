package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

abstract class Mode22BatchCodec extends AbstractBatchCodec {

	private final int defaultBatchSize;

	protected Mode22BatchCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands, int defaultBatchSize) {
		super(codecType, init, adjustments, query, commands);
		this.defaultBatchSize = defaultBatchSize;
	}

	@Override
	protected int determineBatchSize(final String mode) {
		final Integer mode22BatchSize = adjustments.getBatchPolicy().getMode22BatchSize();
		
		if (MODE_22.equals(mode)) {
			return mode22BatchSize == null || mode22BatchSize <= 0 ? defaultBatchSize : mode22BatchSize;
		} else {
			return DEFAULT_BATCH_SIZE;
		}
	}
}
