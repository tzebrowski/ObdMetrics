package org.obd.metrics.command.obd;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class PatternEntry {
	final ObdCommand command;
	final int start;
	final int end;
}