package org.openelm327.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.MultiValuedMap;
import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandReply;
import org.openelm327.core.command.CustomCommand;
import org.openelm327.core.command.DescribeProtocolCommand;
import org.openelm327.core.command.EchoCommand;
import org.openelm327.core.command.EngineTempCommand;
import org.openelm327.core.command.HeadersCommand;
import org.openelm327.core.command.ProtocolCloseCommand;
import org.openelm327.core.command.QueryForPidsCommand;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ReadVoltagetCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.command.SelectProtocolCommand;
import org.openelm327.core.streams.StreamFactory;
import org.openelm327.core.streams.Streams;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class IntegrationTest {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		defaultUsecase();
	}

	static void defaultUsecase() throws IOException, InterruptedException, ExecutionException {
		final Commands commands = new Commands();
		commands.add(new ResetCommand());// reset
		commands.add(new ReadVoltagetCommand());
		commands.add(new CustomCommand("AT L0"));
		commands.add(new HeadersCommand(0));// headers off
		commands.add(new EchoCommand(0));// echo off
		commands.add(new SelectProtocolCommand(0)); // protocol default
		commands.add(new DescribeProtocolCommand());

		// 01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c
		commands.add(new QueryForPidsCommand("00")); // get supported pids 41 00 98 3F 80 10
		
		commands.add(new QueryForPidsCommand("20")); // get supported pids
		commands.add(new QueryForPidsCommand("40")); // get supported pids

		commands.add(new CustomCommand("01 0C")); // engine rpm
		commands.add(new CustomCommand("01 0F")); // air intake
		commands.add(new CustomCommand("01 10")); // maf
		commands.add(new CustomCommand("01 0B")); // intake manifold pressure
		commands.add(new CustomCommand("01 0D")); // vehicle speed
		
		commands.add(new EngineTempCommand());
		commands.add(new EngineTempCommand());
		commands.add(new EngineTempCommand());

		commands.add(new ProtocolCloseCommand()); // protocol close
		commands.add(new QuitCommand());// quite the CommandExecutor

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final CommandReplyCollector dataCollector = new CommandReplyCollector();

		final CommandExecutor commandExecutor = CommandExecutor.builder().streams(streams).commands(commands)
				.subscriber(dataCollector).build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		for (Future<String> result : executorService.invokeAll(Arrays.asList(commandExecutor))) {
			log.info("Result of command executor: ", result.get());
		}

		final MultiValuedMap<Command, CommandReply> data = dataCollector.getData();
		
		data.entries().stream().forEach(k -> {
			log.info("{}", k);
		});
		executorService.shutdown();
	}
}
