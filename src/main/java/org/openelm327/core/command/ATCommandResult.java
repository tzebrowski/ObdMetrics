package org.openelm327.core.command;

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
