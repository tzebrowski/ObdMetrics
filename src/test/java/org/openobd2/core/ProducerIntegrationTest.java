package org.openobd2.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.CommandsProducer;
import org.openobd2.core.ObdDataCollector;
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

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class ProducerIntegrationTest {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		defaultUsecase();
	}

	static void defaultUsecase() throws IOException, InterruptedException, ExecutionException {
		final CommandsBuffer buffer = new CommandsBuffer();
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); //line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default
		
		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final ObdDataCollector collector = new ObdDataCollector();

		final CommandExecutor executor = CommandExecutor.builder().streams(streams).commandsBuffer(buffer)
				.subscriber(collector).build();
		final CommandsProducer producer = CommandsProducer.builder().commands(buffer).build();

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(2);
		newFixedThreadPool.invokeAll(Arrays.asList(executor, producer));
		
		
		final MultiValuedMap<Command, CommandReply> data = collector.getData();
		//get engine temp
		data.get(new EngineTempCommand()).stream().forEach(k -> {
			log.info("{}", k);
		});
		newFixedThreadPool.shutdown();
	}
}
