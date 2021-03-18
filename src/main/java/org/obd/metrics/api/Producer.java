package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;

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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class Producer extends ReplyObserver<Reply<?>> implements Callable<String> {

	@NonNull
	protected StatisticsRegistry statisticsRegistry;

	@NonNull
	protected CommandsBuffer buffer;

	@NonNull
	protected AdaptiveTimeoutPolicy policy;

	@NonNull
	protected Collection<ObdCommand> cycleCommands;

	protected volatile boolean quit = false;

	protected PidDefinition measuredPid;

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
		        SupportedPidsCommand.class.getName(),
		        ObdMetric.class.getName() };
	}

	@Override
	public String call() throws Exception {
		try {
			log.info("Starting Producer thread....");

			var conditionalSleep = ConditionalSleep
			        .builder()
			        .particle(20l)
			        .condition(() -> quit)
			        .build();

			var adaptiveTiming = new AdaptiveTimeout(policy);

			log.info("Timeout: {}ms for expected command frequency: {}, "
			        + "adaptive timing enabled: {}, check interval: {}",
			        adaptiveTiming.getCurrentTimeout(),
			        policy.getCommandFrequency(),
			        policy.isEnabled(),
			        policy.getCheckInterval());

			while (!quit) {

				conditionalSleep.sleep(adaptiveTiming.getCurrentTimeout());

				if (log.isTraceEnabled()) {
					log.trace("Add commands to the buffer: {}", cycleCommands);
				}

				buffer.addAll(cycleCommands);

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
