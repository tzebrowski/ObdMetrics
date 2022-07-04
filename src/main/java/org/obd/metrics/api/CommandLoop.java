package org.obd.metrics.api;

import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.executor.CommandExecutorOrchestrator;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class CommandLoop implements Callable<String> {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 5;
	private final AdapterConnection connection;

	CommandLoop(AdapterConnection connection) {
		this.connection = connection;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");
		final Context context = Context.instance();
		final CommandsBuffer buffer = context.lookup(CommandsBuffer.class).get();

		final CommandExecutorOrchestrator commandsExecutor = new CommandExecutorOrchestrator();

		try (final Connector connector = Connector.builder().connection(connection).build()) {
			context.register(Connector.class, connector);

			while (true) {

				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);

				if (connector.isFaulty()) {
					handleError(null, "Device connection is faulty. Finishing communication.");
					return null;
				} else {
					final Command command = buffer.get();
					final CommandExecutionStatus status = commandsExecutor.run(command);
					if (CommandExecutionStatus.ABORT == status) {
						return null;
					}
				}
			}

		} catch (Throwable e) {
			handleError(e, String.format("Command Loop failed: %s", e.getMessage()));
		} finally {
			log.info("Completed Commmand Loop.");
		}
		return null;
	}

	private void handleError(final Throwable e, final String message) {
		log.error(message, e);

		Context.instance().lookup(EventsPublishlisher.class).ifPresent(p -> {
			p.onError(new Exception(message));
			p.onNext(Reply.builder().command(new QuitCommand()).build());
		});

		Context.instance().lookup(Subscription.class).ifPresent(p -> {
			p.onError(message, e);
		});
	}
}