package org.obd.metrics;

import java.util.List;
import java.util.concurrent.Callable;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.executor.ExecutionContext;
import org.obd.metrics.executor.ExecutionStatus;
import org.obd.metrics.executor.CommandExecutor;
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
	private final Lifecycle lifecycle;
	private EventsPublishlisher<Reply<?>> publisher;
	private final DevicePropertiesReader propertiesReader = new DevicePropertiesReader();
	private final DeviceCapabilitiesReader capabilitiesReader = new DeviceCapabilitiesReader();
	private ExecutionContext executionContext;
	
	@Builder
	static CommandLoop build(
			@NonNull AdapterConnection connection, 
			@NonNull CommandsBuffer buffer,
			@Singular("observer") List<ReplyObserver<Reply<?>>> observers, 
			@NonNull CodecRegistry codecs,
			Lifecycle lifecycle, 
			@NonNull PidDefinitionRegistry pids) {

		final CommandLoop loop = new CommandLoop(connection, buffer, lifecycle);
		loop.publisher = EventsPublishlisher.builder().observers(observers).observer(loop.propertiesReader)
				.observer(loop.capabilitiesReader).build();
		
		loop.executionContext = ExecutionContext.builder()
				.codecRegistry(codecs)
				.pids(pids)
				.publisher(loop.publisher)
				.lifecycle(lifecycle).build();
		
		return loop;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connector connector = Connector.builder().connection(connection).build()) {
			executionContext.setConnector(connector);
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
					
					executionContext.setDeviceCapabilities(capabilitiesReader.getCapabilities());
					executionContext.setDeviceProperties(propertiesReader.getProperties());
					
					final Command command = buffer.get();
					log.trace("Executing the command: {}", command);
					final CommandExecutor executor = CommandExecutor.findBy(command, connector);
					if (ExecutionStatus.ABORT == executor.execute(executionContext, command)) {
						return null;
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