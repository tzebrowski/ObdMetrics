package org.obd.metrics.command.obd;

import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;

public class BatchObdCommand extends ObdCommand implements BatchCodec {

	@Getter
	private final int priority;
	private final BatchCodec delegate;

	public BatchObdCommand(String query, List<ObdCommand> commands, int priority) {
		super(query);
		this.priority = priority;
		this.delegate = BatchCodec.instance(query, commands);
	}

	@Override
	public int getCacheHit(String query) {
		return delegate.getCacheHit(query);
	}

	@Override
	public Map<ObdCommand, String> decode(PidDefinition pid, String message) {
		return delegate.decode(pid, message);
	}

	@Override
	public String toString() {
		return "[priority=" + priority + ", query=" + query + "]";
	}

	@Override
	public List<BatchObdCommand> encode() {
		return delegate.encode();
	}
}
