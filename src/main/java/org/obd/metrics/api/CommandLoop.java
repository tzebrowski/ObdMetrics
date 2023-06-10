package org.obd.metrics.api;

import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.executor.CommandHandler;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public final class CommandLoop extends LifecycleAdapter implements Callable<Void> {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 2;
	private final AdapterConnection connection;
	private volatile boolean isStopped = false;

	public CommandLoop(AdapterConnection connection) {
		this.connection = connection;
	}

	@Override
	public Void call() throws Exception {

		log.info("Starting command executor thread..");
		final Context context = Context.instance();

		final CommandsBuffer buffer = context.resolve(CommandsBuffer.class).get();
		final CommandHandler handler = CommandHandler.of();

		try (final Connector connector = Connector.builder().connection(connection).build()) {
			context.register(Connector.class, connector);

			while (!isStopped) {
				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);

				if (connector.isFaulty()) {
					handleError(null, "Device connection is faulty. Finishing communication.");
					return null;
				} else {

					final Command command = buffer.get();
					final CommandExecutionStatus status = handler.execute(connector, command);
					if (CommandExecutionStatus.ABORT == status) {
						return null;
					}
				}
			}

		} catch (InterruptedException e) {
			log.info("Commmand Loop is interupted");
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