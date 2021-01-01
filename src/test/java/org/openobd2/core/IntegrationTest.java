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
import org.openobd2.core.channel.Channel;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.group.Mode1CommandGroup;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class IntegrationTest extends IntegrationTestBase {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {
		
		final Channel channel = openStream();
		Assertions.assertThat(channel).isNotNull();
		
		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("generic.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

		final CommandsBuffer buffer = new CommandsBuffer(); //Define command buffer
		buffer.add(Mode1CommandGroup.INIT_PROTO_DEFAULT); // Add protocol initialization AT commands
		buffer.add(Mode1CommandGroup.SUPPORTED_PIDS); // Request for supported PID's

		//Read signals from the device
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0C"))); //Engine rpm
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0F"))); //Air intake
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "10"))); //Maf
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0B"))); //Intake manifold pressure
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0D"))); //Behicle speed
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "05"))); //Engine temp

		buffer.add(new QuitCommand());// Last command that will close the communication

		final DataCollector collector = new DataCollector(); //It collects the 

		final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();

		final CommandExecutor executor = CommandExecutor
				.builder()
				.streams(channel)
				.buffer(buffer)
				.subscribe(collector)
				.policy(ExecutorPolicy.builder().frequency(100).build())
				.codecRegistry(codecRegistry)
				.build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));

		final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();

		data.entries().stream().forEach(k -> {
			log.info("{}", k);
		});

		Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
		executorService.shutdown();
		source.close();

	}
}
