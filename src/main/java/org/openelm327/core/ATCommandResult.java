package org.openelm327.core;

import org.openelm327.core.command.ATCommand;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ATCommandResult {
	
	@Getter
	final ATCommand command;
	
	@Getter
	final String raw;

	@Getter
	final long timestamp = System.currentTimeMillis();
	
}
