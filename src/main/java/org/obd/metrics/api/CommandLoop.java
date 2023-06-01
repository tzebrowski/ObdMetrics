package org.obd.metrics.api;

import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.executor.CommandExecutorManager;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public final class CommandLoop implements Callable<Void>, Lifecycle {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 2;
	private final AdapterConnection connection;
	private volatile boolean isStopped = false;
	private final Adjustments adjustments;
	
	public CommandLoop(AdapterConnection connection, Adjustments adjustments) {
		this.connection = connection;
		this.adjustments = adjustments;
	}

	@Override
	public void onStopping() {
		log.info("Received onStopping event. Stopping command loop thread.");
		isStopped = true;
	}

	@Override
	public Void call() throws Exception {

		log.info("Starting command executor thread..");
		final Context context = Context.instance();
		final CommandsBuffer buffer = context.resolve(CommandsBuffer.class).get();

		final CommandExecutorManager commandsExecutor = new CommandExecutorManager(adjustments);
			
		try (final Connector connector = Connector.builder().connection(connection).build()) {
			context.register(Connector.class, connector);
			
			while (!isStopped) {
				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);

				if (connector.isFaulty()) {
					handleError(null, "Device connection is faulty. Finishing communication.");
					return null;
				} else {
					final Command command = buffer.get();
					final CommandExecutionStatus status = commandsExecutor.run(connector, command);
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

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.onError(new Exception(message));
			p.onNext(Reply.builder().command(new QuitCommand()).build());
		});

		Context.instance().resolve(Subscription.class).apply(p -> {
			p.onError(message, e);
		});
	}
}