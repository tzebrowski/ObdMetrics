package org.obd.metrics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.DeviceProperty;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.connection.Connections;
import org.obd.metrics.pid.PidRegistry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandLoop extends ReplyObserver implements Callable<String> {

	private Connection connection;
	private CommandsBuffer buffer;
	private PublishSubject<Reply<?>> publisher = PublishSubject.create();
	private CommandLoopPolicy policy;
	private CodecRegistry codecRegistry;
	private Lifecycle lifecycle;
	private PidRegistry pids;
	private final DeviceProperties deviceProperties = new DeviceProperties();

	@Builder
	static CommandLoop build(@NonNull Connection connection, @NonNull CommandsBuffer buffer,
	        @Singular("observer") List<ReplyObserver> observers, @NonNull CommandLoopPolicy policy,
	        @NonNull CodecRegistry codecRegistry, @NonNull Lifecycle lifecycle, @NonNull PidRegistry pids) {

		var loop = new CommandLoop();
		loop.connection = connection;
		loop.buffer = buffer;
		loop.policy = policy;
		loop.codecRegistry = codecRegistry;
		loop.lifecycle = lifecycle;
		loop.pids = pids;

		if (null == observers || observers.isEmpty()) {
			log.info("No subscriber specified.");
		} else {
			observers.forEach(s -> loop.publisher.subscribe(s));
			loop.publisher.subscribe(loop);// subscribe itself
		}
		return loop;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connections conn = Connections.builder().connection(connection).build()) {
			final CommandExecutor commandExecutor = CommandExecutor.builder().codecRegistry(codecRegistry)
			        .connections(conn).pids(pids).publisher(publisher).lifecycle(lifecycle).build();

			while (true) {

				while (!buffer.isEmpty()) {
					Thread.sleep(policy.getFrequency());
					if (conn.isFaulty()) {
						var message = "Device connection is faulty. Finishing communication.";
						log.error(message);
						publishQuitCommand();
						lifecycle.onError(message, null);
						publisher.onError(new Exception(message));
						return null;
					} else {

						var command = buffer.get();

						log.trace("Executing the command: {}", command);
						if (command instanceof DelayCommand) {
							final DelayCommand delayCommand = (DelayCommand) command;
							TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());
						} else if (command instanceof QuitCommand) {
							log.info("Stopping command executor thread. Finishing communication.");
							publishQuitCommand();
							publisher.onCompleted();
							return null;
						} else if (command instanceof InitCompletedCommand) {
							log.info("Initialization is completed. Found following device properties: {}",
							        deviceProperties.getProperties());
							lifecycle.onConnected(deviceProperties);
						} else {
							commandExecutor.execute(command);
						}
					}
				}

				Thread.sleep(policy.getFrequency());
			}
		} catch (Throwable e) {
			publishQuitCommand();
			var message = String.format("Command executor failed: %s", e.getMessage());
			log.trace(message, e);
			lifecycle.onError(message, e);
		}

		return null;
	}

	@Override
	public void onNext(Reply<?> reply) {
		if (reply.command instanceof DeviceProperty) {
			final DeviceProperty deviceProperty = (DeviceProperty) reply.command;
			if (deviceProperty instanceof Codec<?>) {
				final Object decode = ((Codec<?>) deviceProperty).decode(null, reply.getRaw());
				if (decode == null) {
					deviceProperties.add(deviceProperty.getLabel(), reply.getRaw());
				} else {
					deviceProperties.add(deviceProperty.getLabel(), decode.toString());
				}
			} else {
				deviceProperties.add(deviceProperty.getLabel(), reply.getRaw());
			}
		}
	}

	private void publishQuitCommand() {
		publisher.onNext(Reply.builder().command(new QuitCommand()).build());
	}
}