package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class STNxxBatchCodec extends AbstractBatchCodec {

	private static final int MODE_22_BATCH_SIZE = 10;

	STNxxBatchCodec(final Init init, final Adjustments adjustments, final String query, final List<ObdCommand> commands) {
		super(BatchCodecType.STNxx, init, adjustments, query, commands);
	}

	@Override
	protected BatchObdCommand map(List<ObdCommand> commands, int priority) {
		final StringBuffer query = new StringBuffer();
		query.append("STPX ");

		final String mode = commands.get(0).getPid().getMode();
		init.getHeaders().stream().filter(p -> p.getMode().equals(mode)).findFirst().ifPresent(h -> {
			query.append("H:");
			query.append(h.getHeader());
			query.append(", ");
		});

		final String data = mode + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" "));

		query.append("D:");
		query.append(data);

		if (adjustments.isResponseLengthEnabled()) {
			query.append(", R:");
			query.append(determineNumberOfLines(commands));
		}

		log.info("Build query for STN chip = {}", query);
		final BatchCodec codec = BatchCodec.instance(codecType, init, adjustments, query.toString(), commands);
		return new BatchObdCommand(codec, query.toString(), commands, priority);
	}

	@Override
	protected int determineBatchSize(String mode) {
		return MODE_22.equals(mode) ? MODE_22_BATCH_SIZE : DEFAULT_BATCH_SIZE;
	}
}
