package org.openobd2.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.at.DescribeProtocolCommand;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ProtocolCloseCommand;
import org.openobd2.core.command.at.ReadVoltagetCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.at.SelectProtocolCommand;
import org.openobd2.core.command.obd.mode1.CustomCommand;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.streams.StreamFactory;
import org.openobd2.core.streams.Streams;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class IntegrationTest {

	@Test
	public void pidTest() throws IOException, InterruptedException, ExecutionException {
		final CommandsBuffer buffer = new CommandsBuffer();
		buffer.add(new ResetCommand());// reset
		
		buffer.add(new ReadVoltagetCommand());
		buffer.add(new EchoCommand(0));// echo off
		
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new SelectProtocolCommand(0)); // protocol default
		buffer.add(new DescribeProtocolCommand());

		// 01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c
		buffer.add(new SupportedPidsCommand("00")); // get supported pids 41 00 98 3F 80 10

		buffer.add(new SupportedPidsCommand("20")); // get supported pids
		buffer.add(new SupportedPidsCommand("40")); // get supported pids

		buffer.add(new CustomCommand("0C")); // engine rpm
		buffer.add(new CustomCommand("0F")); // air intake
		buffer.add(new CustomCommand("10")); // maf
		buffer.add(new CustomCommand("0B")); // intake manifold pressure
		buffer.add(new CustomCommand("0D")); // vehicle speed


		buffer.add(new ProtocolCloseCommand()); // protocol close
		buffer.add(new QuitCommand());// quite the CommandExecutor

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bluetooth(obdDongleId);

		final DataCollector collector = new DataCollector();
		final ExecutorPolicy executorPolicy  = ExecutorPolicy.builder().frequency(100).build();
		
		final CommandExecutor executor = CommandExecutor
				.builder()
				.streams(streams)
				.buffer(buffer)
				.subscribe(collector)
				.policy(executorPolicy)
				.build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));

		final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();

		data.entries().stream().forEach(k -> {
			log.info("{}", k);
		});
		
		Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
		executorService.shutdown();
	}

}
