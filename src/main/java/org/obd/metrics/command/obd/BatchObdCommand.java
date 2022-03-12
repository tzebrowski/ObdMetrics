package org.obd.metrics.command.obd;

import java.util.List;

import org.obd.metrics.codec.batch.BatchCodec;

import lombok.Getter;

public class BatchObdCommand extends ObdCommand {

	@Getter
	private final int priority;

	@Getter
	private final BatchCodec codec;

	public BatchObdCommand(String query, List<ObdCommand> commands, int priority) {
		super(query);
		this.priority = priority;
		this.codec = BatchCodec.instance(query, commands);
	}
	
	
	@Override
	public String toString() {
		return "[priority=" + priority + ", query=" + query + "]";
	}
}
