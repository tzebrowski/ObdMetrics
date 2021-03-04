package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;

//its not really a test ;)
public class AlfaIntegrationTest {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {

		final Connection connection = BluetoothConnection.openConnection();
		Assertions.assertThat(connection).isNotNull();

		try (final InputStream alfa = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(alfa).build();

			final CommandsBuffer buffer =  new CommandsBuffer();
			buffer.add(AlfaMed17CommandGroup.CAN_INIT)
				  .add(new ObdCommand(pidRegistry.findBy("194F"))) // Estimated oil Temp
				  .add(new ObdCommand(pidRegistry.findBy("1000"))) // Engine rpm
				  .add(new QuitCommand());// quit the CommandExecutor

			final DataCollector collector = new DataCollector();

			final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();

			final CommandLoop executor = CommandLoop
					.builder()
					.connection(connection)
					.buffer(buffer)
					.pids(pidRegistry)
					.observer(collector)
					.codecRegistry(codecRegistry)
					.lifecycle(Lifecycle.DEFAULT)
					.build();

			final ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(executor));

			final MultiValuedMap<Command, Reply<?>> data = collector.getData();

			data.entries().stream().forEach(k -> {
				System.out.println(k.getValue());
			});

			Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
			executorService.shutdown();
		}
	}
}
