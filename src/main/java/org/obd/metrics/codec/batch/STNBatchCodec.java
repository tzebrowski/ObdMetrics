package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class STNBatchCodec extends AbstractBatchCodec {

	private static final int MAX_BATCH_SIZE = 10;

	STNBatchCodec(final Init init, final Adjustments adjustments, final String query, final List<ObdCommand> commands) {
		super(BatchCodecType.STN, init, adjustments, query, commands);
	}

	@Override
	protected BatchObdCommand map(List<ObdCommand> commands, int priority) {
		final StringBuffer query = new StringBuffer();
		query.append("STPX ");

		final String mode = commands.get(0).getPid().getMode();
		final Optional<Header> h = init.getHeaders().stream().filter(p -> p.getMode().equals(mode)).findFirst();
		if (h.isPresent()) {
			query.append("H:");
			query.append(h.get().getHeader());
			query.append(", ");
		}

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
		return MAX_BATCH_SIZE;
	}

	protected int determineNumberOfLines(final List<ObdCommand> commands) {
		final int length = commands.stream().map(p -> p.getPid().getPid().length() + (2 * p.getPid().getLength()))
				.reduce(0, Integer::sum);

		log.info("Calculated response length: {}", length);

		if (length < 12) {
			return 1;
		} else if (length >= 12 && length <= 24) {
			return 2;
		} else if (length >= 24 && length <= 36) {
			return 3;
		} else if (length >= 36 && length <= 38) {
			return 4;
		} else {
			return 5;
		}
	}

}
