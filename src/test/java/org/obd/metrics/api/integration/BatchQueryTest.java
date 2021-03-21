package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.StreamConnection;
import org.obd.metrics.pid.PidRegistry;

//its not really a test ;)
public class BatchQueryTest {

	@Test
	public void t1() throws IOException, InterruptedException, ExecutionException {

		final StreamConnection connection = BluetoothConnection.openConnection();

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

		final CommandsBuffer buffer = new CommandsBuffer(); // Define command buffer
		buffer.add(Mode1CommandGroup.INIT); // Add protocol initialization AT commands
		buffer.addLast(new ObdCommand("01 0C 10"))
		        .addLast(new ObdCommand("01 0C 10 0B"))
		        .addLast(new ObdCommand("01 0C 10 0B 0D"))
		        .addLast(new ObdCommand("01 0C 10 0B 0D 05"))
		        .addLast(new ObdCommand("01 0C 10 0B 0D 05 0F"))
		        .addLast(new ObdCommand("01 0C 10 0B 0D 05 11"))
		        .addLast(new QuitCommand());// Last command that will close the communication

		final DataCollector collector = new DataCollector(); // It collects the

		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript")
		        .build();

		final CommandLoop executor = CommandLoop
		        .builder()
		        .connection(connection)
		        .buffer(buffer)
		        .observer(collector)
		        .pids(pidRegistry)
		        .codecs(codecRegistry)
		        .lifecycle(Lifecycle.DEFAULT).build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));
		executorService.shutdown();
		source.close();
	}
}
