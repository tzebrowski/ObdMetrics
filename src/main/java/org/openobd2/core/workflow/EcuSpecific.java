package org.openobd2.core.workflow;

import org.openobd2.core.command.group.CommandGroup;

import lombok.Builder;
import lombok.Getter;

@Builder
public class EcuSpecific {

	@Getter
	private String pidFile;
	
	@Getter
	private String mode;
	
	@Getter
	private CommandGroup<?> initSequence;
}
