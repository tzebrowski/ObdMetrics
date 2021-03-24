package org.obd.metrics.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Producer extends ReplyObserver<Reply<?>> implements Callable<String> {

	protected final CommandsBuffer buffer;
	protected final Supplier<Optional<Collection<ObdCommand>>> commandsSupplier;
	protected final AdaptiveTimeout adaptiveTimeout;
	protected final WorkflowContext ctx;
	protected volatile boolean quit = false;

	Producer(StatisticsRegistry statisticsRegistry,
	        CommandsBuffer buffer,
	        Supplier<Optional<Collection<ObdCommand>>> commandsSupplier,
	        WorkflowContext ctx) {
		this.ctx = ctx;
		this.commandsSupplier = commandsSupplier;
		this.buffer = buffer;
		this.adaptiveTimeout = new AdaptiveTimeout(ctx.getAdaptiveTiming(), statisticsRegistry);
	}

	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Recieve command reply: {}", reply);

		if (reply.getCommand() instanceof QuitCommand) {
			log.debug("Producer. Recieved QUIT command.");
			quit = true;
		}
	}

	@Override
	public String[] observables() {
		return new String[] { QuitCommand.class.getName() };
	}

	int executeCnt = 0;

	@Override
	public String call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = ctx.getProducerPolicy();

			log.info("Starting Producer thread. Policy: {}.... ", producerPolicy.toString());

			var conditionalSleep = ConditionalSleep
			        .builder()
			        .particle(20l)
			        .condition(() -> quit)
			        .build();

			adaptiveTimeout.schedule();

			while (!quit) {
				final long currentTimeout = adaptiveTimeout.getCurrentTimeout();
				conditionalSleep.sleep(currentTimeout);
				commandsSupplier.get().ifPresent(commands -> {

					if (ctx.isBatchEnabled() && producerPolicy.isPriorityQueue() && commands.size() > 1) {
						// every 800ms we add all the commands
						if (executeCnt >= (producerPolicy.getLowPriorityCommandFrequencyDelay() / currentTimeout)) {
							buffer.addAll(commands);
							executeCnt = 0;
						} else {
							// add just high priority commands
							// always first command
							buffer.addLast(commands.iterator().next());
							executeCnt++;
						}
					} else {
						buffer.addAll(commands);
						log.trace("Adding commands to the buffer: {}", commands);
					}
				});
			}

		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed Producer thread.");
		}
		return null;
	}
}
