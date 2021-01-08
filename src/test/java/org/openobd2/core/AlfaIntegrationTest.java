package org.openobd2.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.group.AlfaMed17CommandGroup;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidRegistry;

//its not really a test ;)
public class AlfaIntegrationTest extends IntegrationTestBase {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {

		final Connection connection = openConnection();
		Assertions.assertThat(connection).isNotNull();

		try (final InputStream alfa = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(alfa).build();

			final CommandsBuffer buffer =  CommandsBuffer.instance();
			buffer
				.add(AlfaMed17CommandGroup.CAN_INIT)
				.add(new ObdCommand(pidRegistry.findBy("22", "194F"))) // Estimated oil Temp
				.add(new ObdCommand(pidRegistry.findBy("22", "1000"))) // Engine rpm
				.add(new QuitCommand());// quit the CommandExecutor

			final DataCollector collector = new DataCollector();

			final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").pids(pidRegistry).build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(connection)
					.buffer(buffer)
					.subscribe(collector).policy(ExecutorPolicy.builder().frequency(100).build())
					.codecRegistry(codecRegistry)
					.build();

			final ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(executor));

			final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();

			data.entries().stream().forEach(k -> {
				System.out.println(k.getValue());
			});

			Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
			executorService.shutdown();
		}
	}
}
