package org.openelm327.core.command;

import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(of = { "raw", "command", "transformed" })
public final class CommandResult {

	@Getter
	private final Command command;

	@Getter
	private final List<String> transformed;

	@Getter
	private final String raw;

	@Getter
	private final long timestamp;

	@Builder
	public static CommandResult build(Command command, String raw) {

		List<String> converted = Arrays.asList();

		if (raw.startsWith("41")) {
			if (command instanceof Transformation) {
				converted = ((Transformation) command).transform(raw);
			}
		}

		return new CommandResult(command, converted, raw, System.currentTimeMillis());
	}

}
