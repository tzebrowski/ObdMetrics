package org.openobd2.core.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openobd2.core.codec.BatchCommandReplyDecoder;
import org.openobd2.core.pid.PidDefinition;

public class BatchObdCommand extends ObdCommand {
	private final List<PidDefinition> pids;
	private final Map<String, ObdCommand> map = new HashMap<>();

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);
		this.pids = commands.stream().map(p -> p.pid).collect(Collectors.toList());
		commands.stream().forEach(p -> map.put(p.getPid().getPid(), p));
	}

	public Map<ObdCommand, String> decode(String raw) {
		final BatchCommandReplyDecoder commandReplyDecoder = new BatchCommandReplyDecoder();
		final Map<String, String> decode = commandReplyDecoder.decode(pids, raw);
		final Map<ObdCommand, String> ret = new HashMap<>();
		decode.forEach((k, v) -> {
			ret.put(map.get(k), v);
		});

		return ret;
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
