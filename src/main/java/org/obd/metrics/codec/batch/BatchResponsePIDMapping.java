package org.obd.metrics.codec.batch;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class BatchResponsePIDMapping {
	@Getter
	private final ObdCommand command;

	@Getter
	private final int start;

	@Getter
	private final int end;
}