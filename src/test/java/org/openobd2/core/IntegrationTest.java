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
import org.openobd2.core.command.CustomCommand;
import org.openobd2.core.command.DescribeProtocolCommand;
import org.openobd2.core.command.EchoCommand;
import org.openobd2.core.command.EngineTempCommand;
import org.openobd2.core.command.HeadersCommand;
import org.openobd2.core.command.LineFeedCommand;
import org.openobd2.core.command.ProtocolCloseCommand;
import org.openobd2.core.command.QueryForPidsCommand;
import org.openobd2.core.command.QuitCommand;
import org.openobd2.core.command.ReadVoltagetCommand;
import org.openobd2.core.command.ResetCommand;
import org.openobd2.core.command.SelectProtocolCommand;
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
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default
		buffer.add(new DescribeProtocolCommand());

		// 01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c
		buffer.add(new QueryForPidsCommand("00")); // get supported pids 41 00 98 3F 80 10

		buffer.add(new QueryForPidsCommand("20")); // get supported pids
		buffer.add(new QueryForPidsCommand("40")); // get supported pids

		buffer.add(new CustomCommand("01 0C")); // engine rpm
		buffer.add(new CustomCommand("01 0F")); // air intake
		buffer.add(new CustomCommand("01 10")); // maf
		buffer.add(new CustomCommand("01 0B")); // intake manifold pressure
		buffer.add(new CustomCommand("01 0D")); // vehicle speed

		buffer.add(new EngineTempCommand());
		buffer.add(new EngineTempCommand());
		buffer.add(new EngineTempCommand());

		buffer.add(new ProtocolCloseCommand()); // protocol close
		buffer.add(new QuitCommand());// quite the CommandExecutor

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final DataCollector collector = new DataCollector();

		final CommandExecutor executor = CommandExecutor.builder().streams(streams).commandsBuffer(buffer)
				.subscriber(collector).build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));

		final MultiValuedMap<Command, CommandReply> data = collector.getData();

		data.entries().stream().forEach(k -> {
			log.info("{}", k);
		});
		
		Assertions.assertThat(collector.getData().containsKey(new QueryForPidsCommand("00")));
		
		
		
		executorService.shutdown();
	}

}
