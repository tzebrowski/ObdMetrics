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
final class STNxxBatchCodec extends AbstractBatchCodec {

	private static final int PRIORITY_0 = 0;
	private static final int MODE_22_BATCH_SIZE = 10;
	
	STNxxBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
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

	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPririty() {
		if (adjustments.isStnPromoteSlowGroupsEnabled()) {
			final Set<Long> promotedToPriority0 = findPromotedPIDs(MODE_22);
			final Map<String, Map<Integer, List<ObdCommand>>> ret = 
				commands.stream().collect(Collectors.groupingBy(f -> {
					return f.getPid().getMode();
				},
				Collectors.groupingBy(p -> {
					if (promotedToPriority0.contains(p.getPid().getId())) {
						return PRIORITY_0;
					} else {
						return p.getPid().getPriority();
					}
				})));
			return ret;
		} else {
			return commands.stream().collect(Collectors.groupingBy(f -> f.getPid().getMode(),
					Collectors.groupingBy(p -> p.getPid().getPriority())));
		}
	}

	@Override
	protected int determineBatchSize(String mode) {
		return MODE_22.equals(mode) ? MODE_22_BATCH_SIZE : DEFAULT_BATCH_SIZE;
	}
	
	private Set<Long> findPromotedPIDs(String mode) {
		final Set<Long> promotedPIDs = new HashSet<>();
		final int numberOfP0 = (int) commands.stream()
				.filter(p -> p.getMode().equals(mode))
				.filter(p -> p.getPid().getPriority() == PRIORITY_0)
				.count();
		
		final int diffToFill = determineBatchSize(mode) - numberOfP0;
		for (int i = 0; i < commands.size() || (i == diffToFill && diffToFill > 0); i++) {
			if (commands.get(i).getPriority() == 1 || commands.get(i).getPriority() == 2) {
				promotedPIDs.add(commands.get(i).getPid().getId());
			}
		}
		return promotedPIDs;
	}
}
