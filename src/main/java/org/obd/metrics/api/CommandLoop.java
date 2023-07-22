package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.executor.CommandHandler;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class CommandLoop extends LifecycleAdapter implements Callable<Void> {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 2;
	private volatile boolean isStopped = false;

	@Override
	public Void call() throws Exception {

		log.info("Starting command executor thread..");
		final Context context = Context.instance();
		final CommandsBuffer buffer = context.forceResolve(CommandsBuffer.class);
		final ConnectionManager connectionManager = context.forceResolve(ConnectionManager.class);
		final CommandHandler handler = CommandHandler.of();
		
		try {

			while (!isStopped) {
				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);
				try {
					final Connector connector = connectionManager.getConnector();
					if (connector.isFaulty()) {
						Subscription.raiseInternalError("Device connection is faulty.", null);
					} else {
						connectionManager.resetFaultCounter();
						final Command command = buffer.get();
						final CommandExecutionStatus status = handler.execute(connector, command);
						if (CommandExecutionStatus.ABORT == status) {
							return null;
						}
					}
				} catch (IOException e) {
					Subscription.raiseInternalError("IO Exception occured: " + e.getMessage(), e);
				}
			}

		} catch (InterruptedException e) {
			log.info("Commmand Loop is interupted");
		} catch (Throwable e) {
			log.info("Commmand Loop Failed", e);
			Subscription.raiseInternalError(String.format("Command Loop failed: %s", e.getMessage()));
		} finally {
			connectionManager.close();
			log.info("Completed Commmand Loop.");
		}
		return null;
	}
}