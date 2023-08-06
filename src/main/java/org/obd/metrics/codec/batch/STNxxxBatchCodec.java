package org.obd.metrics.codec.batch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class STNxxxBatchCodec extends Mode22BatchCodec {

	private static final int PRIORITY_0 = 0;
	private static final int MODE_22_BATCH_SIZE = 11;

	STNxxxBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		super(BatchCodecType.STNxxx, init, adjustments, query, commands, MODE_22_BATCH_SIZE);
	}

	@Override
	protected BatchObdCommand map(List<ObdCommand> commands, int priority) {

		final StringBuffer query = new StringBuffer();
		query.append("STPX ");

		init.getHeaders().stream().filter(p -> p.getMode().equals(getGroupKey(commands.get(0)))).findFirst()
				.ifPresent(h -> {
					query.append("H:");
					query.append(h.getHeader());
					query.append(", ");
				});

		final String data = commands.get(0).getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" "));

		query.append("D:");
		query.append(data);

		if (adjustments.getBatchPolicy().isResponseLengthEnabled()) {
			query.append(", R:");
			query.append(determineNumberOfLines(commands));
		}

		log.info("Build query for STN chip = {}", query);
		final BatchCodec codec = BatchCodec.instance(codecType, init, adjustments, query.toString(), commands);
		return new BatchObdCommand(codec, query.toString(), commands, priority);
	}

	@Override
	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPriority() {
		if (adjustments.getStNxx().isPromoteSlowGroupsEnabled()) {
			final Set<Long> promotedToPriority0 = findPromotedPIDs(MODE_22);
			final Map<String, Map<Integer, List<ObdCommand>>> ret = commands.stream()
					.collect(Collectors.groupingBy(f -> {
						return getGroupKey(f);
					}, Collectors.groupingBy(p -> {
						if (promotedToPriority0.contains(p.getPid().getId())) {
							return PRIORITY_0;
						} else {
							return p.getPid().getPriority();
						}
					})));
			return ret;
		} else {
			return commands.stream().collect(
					Collectors.groupingBy(f -> getGroupKey(f), Collectors.groupingBy(p -> p.getPid().getPriority())));
		}
	}

	private Set<Long> findPromotedPIDs(String mode) {
		final Set<Long> promotedPIDs = new HashSet<>();
		final int numberOfP0 = (int) commands.stream().filter(p -> p.getMode().equals(mode))
				.filter(p -> p.getPid().getPriority() == PRIORITY_0).count();

		final int batchSize = determineBatchSize(mode);
		log.info("Calculated batchSize for STNxxx extension encoder={}", batchSize);

		int diffToFill = determineBatchSize(mode) - numberOfP0;

		for (int i = 0; i < commands.size() || (i == diffToFill && diffToFill > 0 && diffToFill < commands.size() ); i++) {
			if (commands.get(i).getPriority() == 1 || commands.get(i).getPriority() == 2) {
				promotedPIDs.add(commands.get(i).getPid().getId());
			}
		}
		return promotedPIDs;
	}
}
