package org.obd.metrics.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.obd.metrics.AdaptiveTimeoutPolicy;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Producer extends ReplyObserver<Reply<?>> implements Callable<String> {

	protected final StatisticsRegistry statisticsRegistry;
	protected final CommandsBuffer buffer;
	protected final Supplier<Optional<Collection<ObdCommand>>> commandsSupplier;
	protected final AdaptiveTimeout adaptiveTiming;

	protected volatile boolean quit = false;

	Producer(StatisticsRegistry statisticsRegistry, CommandsBuffer buffer, AdaptiveTimeoutPolicy policy,
	        Supplier<Optional<Collection<ObdCommand>>> commandsSupplier) {
		this.statisticsRegistry = statisticsRegistry;
		this.commandsSupplier = commandsSupplier;
		this.buffer = buffer;
		this.adaptiveTiming = new AdaptiveTimeout(policy);
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

	@Override
	public String call() throws Exception {
		try {
			log.info("Starting Producer thread.... ");

			var conditionalSleep = ConditionalSleep
			        .builder()
			        .particle(20l)
			        .condition(() -> quit)
			        .build();

			while (!quit) {
				conditionalSleep.sleep(adaptiveTiming.getCurrentTimeout());
				commandsSupplier.get().ifPresent(commands -> {

					if (log.isTraceEnabled()) {
						log.trace("Adding commands to the buffer: {}", commands);
					}

					buffer.addAll(commands);
					adaptiveTiming.update(statisticsRegistry.getRandomRatePerSec());
				});

			}
		} catch (Throwable e) {
			log.error("Producer failed.", e);
		} finally {
			log.info("Completed Producer thread.");
		}
		return null;
	}
}
