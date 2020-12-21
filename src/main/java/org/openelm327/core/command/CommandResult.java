package org.openelm327.core.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Builder
@ToString(of= {"raw", "command"})
public class CommandResult {
	
	@Getter
	final Command command;
	
	@Getter
	final String raw;

	@Getter
	final long timestamp = System.currentTimeMillis();
	
}
