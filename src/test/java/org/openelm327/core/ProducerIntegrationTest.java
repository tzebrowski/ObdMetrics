package org.openelm327.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandReply;
import org.openelm327.core.command.EchoCommand;
import org.openelm327.core.command.EngineTempCommand;
import org.openelm327.core.command.HeadersCommand;
import org.openelm327.core.command.LineFeedCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.command.SelectProtocolCommand;
import org.openelm327.core.streams.StreamFactory;
import org.openelm327.core.streams.Streams;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class ProducerIntegrationTest {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		defaultUsecase();
	}

	static void defaultUsecase() throws IOException, InterruptedException, ExecutionException {
		final CommandsBuffer commands = new CommandsBuffer();
		commands.add(new ResetCommand());// reset
		commands.add(new LineFeedCommand(0)); //line feed off
		commands.add(new HeadersCommand(0));// headers off
		commands.add(new EchoCommand(0));// echo off
		commands.add(new SelectProtocolCommand(0)); // protocol default
		
		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final CommandReplyCollector dataCollector = new CommandReplyCollector();

		final CommandExecutor commandExecutor = CommandExecutor.builder().streams(streams).commandsBuffer(commands)
				.subscriber(dataCollector).build();
		final CommandsPublisher commandsPublisher = CommandsPublisher.builder().commands(commands).build();

		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(2);
		newFixedThreadPool.invokeAll(Arrays.asList(commandExecutor, commandsPublisher));
		
		
		final MultiValuedMap<Command, CommandReply> data = dataCollector.getData();
		//get engine temp
		data.get(new EngineTempCommand()).stream().forEach(k -> {
			log.info("{}", k);
		});
		newFixedThreadPool.shutdown();
	}
}
