package org.openobd2.core.command.obd;

import java.util.List;
import java.util.Map;

import org.openobd2.core.codec.BatchCommandReplyDecoder;
import org.openobd2.core.codec.Codec;

public class BatchObdCommand extends ObdCommand implements Codec<Map<ObdCommand, String>>{

	private final List<ObdCommand> commands;

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);
		this.commands = commands;
	}

	@Override
	public Map<ObdCommand, String> decode(String raw) {
		return new BatchCommandReplyDecoder().decode(commands, raw);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
