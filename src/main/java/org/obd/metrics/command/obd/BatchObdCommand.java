package org.obd.metrics.command.obd;

import java.util.List;

import org.obd.metrics.codec.batch.BatchCodec;

import lombok.Getter;

public class BatchObdCommand extends ObdCommand {

	@Getter
	private final int priority;

	@Getter
	private final BatchCodec codec;
	private final String mode;
	private final String canMode;
	
	public BatchObdCommand(final BatchCodec codec, final String query, final List<ObdCommand> commands,
			final int priority) {
		super(query);
		this.priority = priority;
		this.codec = codec;
		this.mode = commands.get(0).getMode();
		this.canMode = commands.get(0).getCanMode();
	}

	@Override
	public String getCanMode() {
		return canMode;
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
