package org.openobd2.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.EchoCommand;
import org.openobd2.core.command.EngineTempCommand;
import org.openobd2.core.command.HeadersCommand;
import org.openobd2.core.command.LineFeedCommand;
import org.openobd2.core.command.ResetCommand;
import org.openobd2.core.command.SelectProtocolCommand;
import org.openobd2.core.streams.StreamFactory;
import org.openobd2.core.streams.Streams;

//its not really a test ;)
public class ProducerIntegrationTest {

	@Test
	public void producerTest() throws IOException, InterruptedException, ExecutionException {
		final CommandsBuffer buffer = new CommandsBuffer();
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final DataCollector collector = new DataCollector();

		final CommandExecutor executor = CommandExecutor.builder().streams(streams).commandsBuffer(buffer)
				.subscriber(collector).build();
		
		
		int numOfCommands = 50;
		final CommandsProducer producer = CommandsProducer.builder().commands(buffer).numOfCommands(numOfCommands).build();

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(2);
		newFixedThreadPool.invokeAll(Arrays.asList(executor, producer));

		final MultiValuedMap<Command, CommandReply> data = collector.getData();
		Assertions.assertThat(data.containsKey(new EngineTempCommand()));
		
		final Collection<CommandReply> collection = data.get(new EngineTempCommand());
		Assertions.assertThat(collection).hasSize(numOfCommands);
		
		newFixedThreadPool.shutdown();
	}

}
