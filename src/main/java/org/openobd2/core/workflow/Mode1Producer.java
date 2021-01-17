package org.openobd2.core.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class Mode1Producer extends CommandReplySubscriber implements Callable<String>, Batchable {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;

	@NonNull
	private final PidRegistry pidRegistry;

	@Default
	private final Set<ObdCommand> cycleCommands = new HashSet();

	@Default
	private volatile boolean quit = false;

	private boolean batchEnabled;

	private final Set<String> selectedPids;

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(CommandReply<?> reply) {
		log.trace("Recieve command reply: {}", reply);

		if (reply.getCommand() instanceof SupportedPidsCommand) {
			try {
				final List<String> value = (List<String>) reply.getValue();
				if (value != null) {
					List<ObdCommand> cmds = value.stream()
							.filter(p -> selectedPids.isEmpty() ? true : selectedPids.contains(p)).map(pid -> {
								return toObdCommand(pid);
							}).filter(p -> p != null).collect(Collectors.toList());

					if (batchEnabled) {
						cmds = toBatch(cmds);
					}

					cycleCommands.addAll(cmds);

					log.info("Built list of supported PIDs : {}", cycleCommands);
				}

			} catch (Throwable e) {
				log.error("Failed to read supported pids", e);
			}
		} else if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		while (!quit) {

			TimeUnit.MILLISECONDS.sleep(policy.getDelayBeforeInsertingCommands());
			if (cycleCommands.isEmpty()) {
				TimeUnit.MILLISECONDS.sleep(policy.getEmptyBufferSleepTime());
			} else {
				buffer.addAll(cycleCommands);
			}
		}
		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}

	private ObdCommand toObdCommand(String pid) {
		final PidDefinition pidDefinition = pidRegistry.findBy(pid);
		if (pidDefinition == null) {
			log.warn("No pid definition found for pid: {}", pid);
			return null;
		} else {
			return new ObdCommand(pidDefinition);
		}
	}
}
