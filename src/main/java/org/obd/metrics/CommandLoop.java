package org.obd.metrics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandLoop implements Callable<String> {

	private final AdapterConnection connection;
	private final CommandsBuffer buffer;
	private final CodecRegistry codecs;
	private final Lifecycle lifecycle;
	private final PidDefinitionRegistry pids;
	private EventsPublishlisher<Reply<?>> publisher;
	private final DevicePropertiesReader propertiesReader = new DevicePropertiesReader();
	private final DeviceCapabilitiesReader capabilitiesReader = new DeviceCapabilitiesReader();

	@Builder
	static CommandLoop build(@NonNull AdapterConnection connection, @NonNull CommandsBuffer buffer,
			@Singular("observer") List<ReplyObserver<Reply<?>>> observers, @NonNull CodecRegistry codecs,
			Lifecycle lifecycle, @NonNull PidDefinitionRegistry pids) {

		final CommandLoop loop = new CommandLoop(connection, buffer, codecs, lifecycle, pids);
		loop.publisher = EventsPublishlisher.builder().observers(observers).observer(loop.propertiesReader)
				.observer(loop.capabilitiesReader).build();
		return loop;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connector connector = Connector.builder().connection(connection).build()) {
			final CommandExecutor commandExecutor = CommandExecutor.builder().codecRegistry(codecs).connector(connector)
					.pids(pids).publisher(publisher).lifecycle(lifecycle).build();

			while (true) {
				Thread.sleep(5);
				if (connector.isFaulty()) {
					final String message = "Device connection is faulty. Finishing communication.";
					log.error(message);
					publishQuitCommand();
					lifecycle.onError(message, null);
					publisher.onError(new Exception(message));
					return null;
				} else {
					final Command command = buffer.get();
					log.trace("Executing the command: {}", command);
					if (command instanceof DelayCommand) {
						final DelayCommand delayCommand = (DelayCommand) command;
						TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());
					} else if (command instanceof QuitCommand) {
						log.info("Stopping Command Loop thread. Finishing communication.");
						publishQuitCommand();
						publisher.onCompleted();
						return null;
					} else if (command instanceof InitCompletedCommand) {

						log.info("Initialization is completed.");
						log.info("Found device properties: {}", propertiesReader.getProperties());
						log.info("Found device capabilities: {}", capabilitiesReader.getCapabilities());

						lifecycle.onRunning(new DeviceProperties(propertiesReader.getProperties(),
								capabilitiesReader.getCapabilities()));
					} else {
						commandExecutor.execute(command);
					}
				}
			}

		} catch (Throwable e) {
			publishQuitCommand();
			final String message = String.format("Command Loop failed: %s", e.getMessage());
			log.error(message, e);
			lifecycle.onError(message, e);
		} finally {
			log.info("Completed Commmand Loop.");
		}
		return null;
	}

	private void publishQuitCommand() {
		publisher.onNext(Reply.builder().command(new QuitCommand()).build());
	}
}