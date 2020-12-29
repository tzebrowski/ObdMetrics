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
import org.openobd2.core.channel.bt.BluetoothStream;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.CommandSet;
import org.openobd2.core.command.at.ProtocolCloseCommand;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class IntegrationTest {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {
		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("generic.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

		final CommandsBuffer buffer = new CommandsBuffer();
		buffer.add(CommandSet.INIT_PROTO_DEFAULT);
		buffer.add(CommandSet.MODE1_SUPPORTED_PIDS);
		
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0C"))); // engine rpm
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0F"))); // air intake
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "10"))); // maf
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0B"))); // intake manifold pressure
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0D"))); // vehicle speed
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "0D"))); // vehicle speed
		buffer.add(new ObdCommand(pidRegistry.findBy("01", "05"))); // engine temp

		buffer.add(new ProtocolCloseCommand()); // protocol close
		buffer.add(new QuitCommand());// quite the CommandExecutor

		final Channel streams = BluetoothStream.builder().adapter("AABBCC112233").build();

		final DataCollector collector = new DataCollector();

		final CodecRegistry codecRegistry = CodecRegistry.builder().pidRegistry(pidRegistry).build();

		final CommandExecutor executor = CommandExecutor.builder().streams(streams).buffer(buffer).subscribe(collector)
				.policy(ExecutorPolicy.builder().frequency(100).build()).codecRegistry(codecRegistry).build();

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
