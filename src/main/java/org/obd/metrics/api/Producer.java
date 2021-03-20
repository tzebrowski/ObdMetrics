package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.obd.metrics.AdaptiveTimeoutPolicy;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Producer extends ReplyObserver<Reply<?>> implements Callable<String> {

	protected final StatisticsRegistry statisticsRegistry;
	protected final CommandsBuffer buffer;
	protected final Supplier<Collection<ObdCommand>> commandsSupplier;

	protected final AdaptiveTimeout adaptiveTiming;

	protected volatile boolean quit = false;
	protected PidDefinition measuredPid;

	Producer(StatisticsRegistry statisticsRegistry, CommandsBuffer buffer, AdaptiveTimeoutPolicy policy,
	        Supplier<Collection<ObdCommand>> commandsSupplier) {
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
		} else if (reply instanceof ObdMetric && measuredPid == null) {
			if (!(reply.getCommand() instanceof SupportedPidsCommand)) {
				measuredPid = ((ObdMetric) reply).getCommand().getPid();
			}
		}
	}

	@Override
	public String[] observables() {
		return new String[] {
		        QuitCommand.class.getName(),
		        ObdMetric.class.getName() };
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
				final Collection<ObdCommand> commands = commandsSupplier.get();

				if (log.isTraceEnabled()) {
					log.trace("Adding commands to the buffer: {}", commands);
				}

				buffer.addAll(commands);

				if (null != measuredPid) {
					final double ratePerSec = statisticsRegistry.getRatePerSec(measuredPid);
					adaptiveTiming.update(ratePerSec);
				}
			}
		} catch (Throwable e) {
			log.error("Producer failed.", e);
		} finally {
			log.info("Completed Producer thread.");
		}
		return null;
	}
}
