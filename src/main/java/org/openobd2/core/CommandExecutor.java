package org.openobd2.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.codec.batch.Batchable;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.process.DelayCommand;
import org.openobd2.core.command.process.InitCompletedCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandExecutor implements Callable<String> {

	private static final String NO_DATA = "no data";
	private static final String STOPPED = "stopped";
	private static final String UNABLE_TO_CONNECT = "unable to connect";

	private Connection connection;
	private CommandsBuffer buffer;
	private PublishSubject<CommandReply<?>> publisher = PublishSubject.create();
	private ExecutorPolicy policy;
	private CodecRegistry codecRegistry;
	private StatusObserver statusObserver;

	@Builder
	static CommandExecutor build(@NonNull Connection connection, 
			@NonNull CommandsBuffer buffer,
			@Singular("subscribe") List<Observer<CommandReply<?>>> subscribe, 
			@NonNull ExecutorPolicy policy,
			@NonNull CodecRegistry codecRegistry, 
			@NonNull StatusObserver statusObserver) {

		final CommandExecutor commandExecutor = new CommandExecutor();
		commandExecutor.connection = connection;
		commandExecutor.buffer = buffer;
		commandExecutor.policy = policy;
		commandExecutor.codecRegistry = codecRegistry;
		commandExecutor.statusObserver = statusObserver;

		if (null == subscribe || subscribe.isEmpty()) {
			log.info("No subscriber specified.");
		} else {
			subscribe.forEach(s -> commandExecutor.publisher.subscribe(s));
		}
		return commandExecutor;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connections conn = Connections.builder().connection(connection).build()) {
			while (true) {
				Thread.sleep(policy.getFrequency());
				while (!buffer.isEmpty()) {

					if (conn.isFaulty()) {
						final String message = "Device connection is faulty. Finishing communication.";
						log.error(message);
						publishQuitCommand();
						statusObserver.onError(message, null);
						return null;
					} else {

						final Command command = buffer.get();

						if (command instanceof DelayCommand) {
							final DelayCommand delayCommand = (DelayCommand) command;
							TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());

						} else if (command instanceof QuitCommand) {
							log.info("Stopping command executor thread. Finishing communication.");
							publishQuitCommand();
							return null;

						} else if (command instanceof InitCompletedCommand) {
							log.info("Initialization is completed.");
							statusObserver.onConnected();
						} else {

							TimeUnit.MILLISECONDS.sleep(policy.getDelayBeforeExecution());
							final String data = execute(conn, command);
							
							if (null == data || data.length() == 0) {
								log.debug("Recieved no data.");
								continue;
							} else if (data.contains(STOPPED)) {
								log.debug("Communication with the device is stopped.");
								statusObserver.onError("Stopped", null);
							} else if (data.contains(NO_DATA)) {
								log.debug("Recieved no data.");
							} else if (data.contains(UNABLE_TO_CONNECT)) {
								log.error("Unable to connnect do device.");
								statusObserver.onError("Unable to connect.", null);
							} else if (command instanceof Batchable) {
								((Batchable) command).decode(data).forEach(this::publishCommandReply);
								continue;
							}
							publishCommandReply(command, data);
						}
					}
				}
			}
		} catch (Throwable e) {
			publishQuitCommand();
			final String message = String.format("Command executor failed: %s", e.getMessage());
			log.error(message, e);
			statusObserver.onError(message, e);
		}

		return null;
	}

	private Object decode(Command c, String v) {
		return codecRegistry.findCodec(c).map(p -> p.decode(v)).orElse(null);
	}

	private void publishCommandReply(final Command command, final String data) {
		publisher.onNext(CommandReply.builder().command(command).raw(data).value(decode(command, data)).build());
	}

	private void publishQuitCommand() {
		publisher.onNext(CommandReply.builder().command(new QuitCommand()).build());
	}

	private String execute(Connections connections, Command command) throws InterruptedException {
		connections.transmit(command);
		return connections.receive();
	}
}