package org.obd.metrics.api;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	GenericWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver observer,
	        Lifecycle lifecycle, ProducerPolicy producerPolicy) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle, producerPolicy);
	}

	@Override
	Producer getProducer(WorkflowContext ctx) {
		final Set<ObdCommand> cycleCommands = getCycleCommands(ctx);
		log.info("Generic workflow selected commands: {}", cycleCommands);
		return new Producer(statisticsRegistry, comandsBuffer, producerPolicy, cycleCommands);
	}

	@Override
	void init() {
		lifecycle.onConnecting();
		comandsBuffer.clear();
		pidSpec.getSequences().forEach(comandsBuffer::add);
		comandsBuffer.addLast(new InitCompletedCommand());
	}

	private Set<ObdCommand> getCycleCommands(WorkflowContext ctx) {
		final Set<Long> newFilter = ctx.filter == null ? Collections.emptySet() : ctx.filter;

		final Set<ObdCommand> cycleCommands = newFilter.stream().map(pid -> {
			final PidDefinition pidDefinition = pidRegistry.findBy(pid);
			if (pidDefinition == null) {
				log.warn("No pid definition found for pid: {}", pid);
				return null;
			} else {
				return new ObdCommand(pidDefinition);
			}
		}).filter(p -> p != null).collect(Collectors.toSet());
		return cycleCommands;
	}
}
