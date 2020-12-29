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
import org.openobd2.core.command.AlfaMed17CommandSet;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

//its not really a test ;)
public class AlfaIntegrationTest {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {

		try (final InputStream alfa = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(alfa).build();

			final CommandsBuffer buffer = new CommandsBuffer();

			buffer.add(AlfaMed17CommandSet.CAN_INIT);

			// request the data
			buffer.add(new ObdCommand(pidRegistry.findBy("22", "194F"))); // 62194f2e05.
			buffer.add(new ObdCommand(new PidDefinition(0, "", "22", "F1A5", "", "", "", ""))); // 008.0:62F1A5080719.1:8986.
			buffer.add(new ObdCommand(new PidDefinition(0, "", "22", "1000", "", "", "", ""))); // 6210000000.
			buffer.add(new ObdCommand(new PidDefinition(0, "", "22", "186B", "", "", "", ""))); // 62186B58..
			buffer.add(new ObdCommand(new PidDefinition(0, "", "22", "183F", "", "", "", ""))); // 62183F7B..

			buffer.add(new QuitCommand());// quit the CommandExecutor

			final Channel streams = BluetoothStream.builder().adapter("AABBCC112233").build();

			final DataCollector collector = new DataCollector();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.streams(streams)
					.buffer(buffer)
					.subscribe(collector)
					.policy(ExecutorPolicy.builder().frequency(100).build())
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
