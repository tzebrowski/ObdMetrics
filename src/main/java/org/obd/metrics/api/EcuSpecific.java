package org.obd.metrics.api;

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
