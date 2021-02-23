package org.obd.metrics.api;

import java.util.List;

import org.obd.metrics.command.group.CommandGroup;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Builder
public class EcuSpecific {

	@Getter
	@NonNull
	@Singular("pidFile")
	private List<String> files;

	@Getter
	@NonNull
	@Singular("initSequence")
	private List<CommandGroup<?>> sequences;

}
