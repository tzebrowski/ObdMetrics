package org.openelm327.core.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor()
@ToString(of = { "raw", "command", "values" })
public final class CommandReply {

	@Getter
	private final Command command;

	@Getter
	private final List<String> values;

	@Getter
	private final String raw;

	@Getter
	private final long timestamp = System.currentTimeMillis();
}
