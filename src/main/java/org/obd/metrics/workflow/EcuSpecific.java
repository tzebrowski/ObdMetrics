package org.obd.metrics.workflow;

import org.obd.metrics.command.group.CommandGroup;

import lombok.Builder;
import lombok.Getter;

@Builder
public class EcuSpecific {

	@Getter
	private String pidFile;
	
	@Getter
	private CommandGroup<?> initSequence;
}
