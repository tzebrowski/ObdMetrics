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
import org.openobd2.core.command.at.CustomATCommand;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ProtocolCloseCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidRegistry;
import org.openobd2.core.streams.Streams;
import org.openobd2.core.streams.bt.BluetoothStream;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class AlfaIntegrationTest {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {
		final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json");

		final InputStream alfa = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).source(alfa).build();

		
		final CommandsBuffer buffer = new CommandsBuffer();
		
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(1));// headers off
		buffer.add(new EchoCommand(0));// echo off

		//https://www.scantool.net/scantool/downloads/234/stn1100-frpm-preliminary.pdf
		buffer.add(new CustomATCommand("ATSPB"));//set protocol to B
		buffer.add(new CustomATCommand("ATCP18"));//Set CAN priority to 18 (29 bit only)
		buffer.add(new CustomATCommand("ATCRA18DAF110"));//Set CAN hardware filter,18DAF110

		//Set the header of transmitted OBD messages to header. Exactly what this command does depends on the currently selected protocol
		buffer.add(new CustomATCommand("ATSHDA10F1"));//Set CAN request message header, DA10F1 
		
		buffer.add(new CustomATCommand("ATAT0"));//Adaptive timing off, auto1*, auto2
		
		buffer.add(new CustomATCommand("ATST19"));//Set OBD response timeout.

		
		
		buffer.add(new ProtocolCloseCommand()); // protocol close
		buffer.add(new QuitCommand());// quite the CommandExecutor

		final Streams streams = BluetoothStream.builder().adapter("AABBCC112233").build();

		final DataCollector collector = new DataCollector();
		
		
		final CodecRegistry codecRegistry = CodecRegistry.builder().pidRegistry(pidRegistry).build();
		
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
		source.close();
		
	}
}
