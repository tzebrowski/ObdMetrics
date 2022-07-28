package org.obd.metrics.command.obd;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.batch.BatchCodec;

import lombok.Getter;

public class BatchObdCommand extends ObdCommand {

	@Getter
	private final int priority;

	@Getter
	private final BatchCodec codec;
	private final String mode;

	public BatchObdCommand(final Adjustments adjustments, final String query, final List<ObdCommand> commands,
			final int priority) {
		super(query);
		this.priority = priority;
		this.codec = BatchCodec.instance(adjustments, query, commands);
		this.mode = commands.get(0).getMode();
	}

	@Override
	public String getMode() {
		return mode;
	}

	@Override
	public String toString() {
		return "[priority=" + priority + ", query=" + query + "]";
	}
}
