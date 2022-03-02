package org.obd.metrics.codec.batch;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchCommandPatternEntry {
	@Getter
	private final ObdCommand command;

	@Getter
	private final int start;
	
	@Getter
	private final int end;
}