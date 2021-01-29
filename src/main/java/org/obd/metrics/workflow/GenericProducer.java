package org.obd.metrics.workflow;

import java.util.Set;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.NonNull;

final class GenericProducer extends Producer {

	public GenericProducer(@NonNull CommandsBuffer buffer, @NonNull ProducerPolicy policy,
			Set<ObdCommand> cycleCommands) {
		super(buffer, policy);
		this.cycleCommands = cycleCommands;
	}
}
