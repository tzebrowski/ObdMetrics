package org.obd.metrics.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CommandExecutionStatus {

	public final static CommandExecutionStatus OK = new CommandExecutionStatus(null);
	public final static CommandExecutionStatus ABORT = new CommandExecutionStatus(null);
	public final static CommandExecutionStatus ERR_LVRESET = new CommandExecutionStatus("LVRESET");
	public final static CommandExecutionStatus ERR_TIMEOUT = new CommandExecutionStatus("TIMEOUT");
	
	@Getter
	private final String message;	
}