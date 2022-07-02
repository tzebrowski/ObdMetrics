package org.obd.metrics.api;

import java.util.List;
import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.executor.CommandExecutor;
import org.obd.metrics.executor.ExecutionContext;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandLoop implements Callable<String> {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 5;
	private final AdapterConnection connection;
	private final CommandsBuffer buffer;
	private final Lifecycle lifecycle;
	private EventsPublishlisher<Reply<?>> publisher;
	private final DevicePropertiesReader propertiesReader = new DevicePropertiesReader();
	private final DeviceCapabilitiesReader capabilitiesReader = new DeviceCapabilitiesReader();
	private ExecutionContext executionContext;

	CommandLoop(AdapterConnection connection, 
			CommandsBuffer buffer, 
			Lifecycle lifecycle,
			CodecRegistry codecs,
			PidDefinitionRegistry pids,
			List<ReplyObserver<Reply<?>>> observers) {
	
		this.connection = connection;
		this.buffer = buffer;
		this.lifecycle = lifecycle;
		this.publisher = EventsPublishlisher
				.builder()
				.observers(observers)
				.observer(propertiesReader)
				.observer(capabilitiesReader).build();
		this.executionContext = ExecutionContext
					.builder()
					.codecRegistry(codecs)
					.pids(pids)
					.publisher(publisher)
				.lifecycle(lifecycle).build();
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connector connector = Connector.builder().connection(connection).build()) {
			executionContext.setConnector(connector);
			while (true) {
				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);
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
					final CommandExecutionStatus status = CommandExecutor.run(executionContext, command);
					if (CommandExecutionStatus.ABORT == status) {
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