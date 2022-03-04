package org.obd.metrics;

import org.obd.metrics.command.Command;
import org.obd.metrics.model.RawMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	@Getter
	protected final T command;

	@Getter
	protected final RawMessage raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();
}
