package org.openobd2.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.process.DelayCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@AllArgsConstructor
public final class CommandExecutor implements Callable<String> {

	private static final String NO_DATA = "NO DATA";
	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	
	private final Connection connection;
	private final CommandsBuffer commandsBuffer;
	private final PublishSubject<CommandReply<?>> publisher = PublishSubject.create();
	private final ExecutorPolicy executorPolicy;
	private final CodecRegistry codecRegistry;
	private final StatusListener state;
	
	@Builder
	static CommandExecutor build(@NonNull Connection connection, @NonNull CommandsBuffer buffer,
			@Singular("subscribe") List<Observer<CommandReply<?>>> subscribe, @NonNull ExecutorPolicy policy,
			@NonNull CodecRegistry codecRegistry,@NonNull StatusListener state) {

		final CommandExecutor commandExecutor = new CommandExecutor(connection, buffer, policy, codecRegistry,state);

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
			state.onConnected();
			while (true) {
				Thread.sleep(executorPolicy.getFrequency());
				while (!commandsBuffer.isEmpty()) {

					final Command command = commandsBuffer.get();
					if (command instanceof DelayCommand) {
						final DelayCommand delayCommand = (DelayCommand) command;
						TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());
						
					} else if (command instanceof QuitCommand) {
						log.info("Stopping command executor thread. Finishing communication.");
						publisher.onNext(CommandReply.builder().command(command).build());
						return "stopped";

					} else {
						if (conn.isFaulty()) {
							TimeUnit.MILLISECONDS.sleep(500);
							state.onError("Connection is faulty");
						} else {
							TimeUnit.MILLISECONDS.sleep(20);
							final String data = exchangeCommand(conn, command);
							if (null == data) {
								continue;
							} else if (data.contains(STOPPED)) {
								log.debug("Communication with the device is stopped.");
								state.onError("Stopped");
							} else if (data.contains(NO_DATA)) {
								log.debug("No data recieved.");
							} else if (data.contains(UNABLE_TO_CONNECT)) {
								log.error("Unable to connnect do device.");
								state.onError("Unable to connect");
							}

							publisher.onNext(
									CommandReply
									.builder()
									.command(command)
									.raw(data)
									.value(codecRegistry.findCodec(command).map(p -> p.decode(data)).orElse(null))
									.build());
						}
					}
				}
			}
		} catch (Throwable e) {
			log.error("Command executor failed.", e);
			state.onError("Connection issue");
		}
		return null;
	}

	String exchangeCommand(Connections connections, Command command) throws InterruptedException {
		connections.transmit(command);
		return connections.receive();
	}
}