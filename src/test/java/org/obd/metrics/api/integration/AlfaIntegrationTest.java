package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.DataCollector;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.DefaultRegistry;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class AlfaIntegrationTest {

	@Test
	public void smokeTest() throws IOException, InterruptedException, ExecutionException {

		try (final InputStream alfa = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")) {

			// Create an instance of PidRegistry that hold PID's configuration
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(alfa).build();

			// Create an instance of CommandBuffer that holds the commands executed against
			// OBD Adapter
			CommandsBuffer buffer = CommandsBuffer.instance();

			// Query for specified PID's like: Estimated oil temperature
			buffer.add(AlfaMed17CommandGroup.CAN_INIT)
			        .addLast(new ObdCommand(pidRegistry.findBy("194F"))) // Estimated oil temp
			        .addLast(new ObdCommand(pidRegistry.findBy("1000"))) // Engine rpm
			        .addLast(new QuitCommand());// quit the CommandExecutor

			// Create an instance of DataCollector that receives the OBD Metrics
			DataCollector collector = new DataCollector();

			// Create an instance of CodecRegistry that will handle decoding incoming raw
			// OBD frames
			DefaultRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();

			// Connection for an OBD adapter
			AdapterConnection connection = BluetoothConnection.openConnection();

			// commandLoop that glue all the ingredients
			CommandLoop commandLoop = CommandLoop
			        .builder()
			        .connection(connection)
			        .buffer(buffer)
			        .pids(pidRegistry)
			        .observer(collector)
			        .codecs(codecRegistry)
			        .build();

			ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(commandLoop));

			// ensure we receive metric
			Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
			executorService.shutdown();
		}
	}
}
