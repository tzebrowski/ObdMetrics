package org.obd.metrics.api;

import java.net.URL;
import java.util.List;

import org.obd.metrics.command.group.CommandGroup;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Builder
public class PidSpec {

	@Getter
	@NonNull
	@Singular("pidFile")
	private List<URL> sources;

	@Getter
	@NonNull
	@Singular("initSequence")
	private List<CommandGroup<?>> sequences;
}
