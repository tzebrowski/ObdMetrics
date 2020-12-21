package org.openelm327.core;

import java.io.IOException;

import org.openelm327.core.command.CustomCommand;
import org.openelm327.core.command.DescribeProtocolCommand;
import org.openelm327.core.command.EchoCommand;
import org.openelm327.core.command.HeadersCommand;
import org.openelm327.core.command.ProtocolCloseCommand;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ReadVoltagetCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.command.SelectProtocolCommand;
import org.openelm327.core.streams.StreamFactory;
import org.openelm327.core.streams.Streams;

public class TestController {

	public static void main(String[] args) throws IOException, InterruptedException {

		final Commands commandQueue = new Commands();
		commandQueue.add(new ResetCommand());// reset
		commandQueue.add(new ReadVoltagetCommand());
		commandQueue.add(new CustomCommand("AT L0"));
		commandQueue.add(new HeadersCommand(0));// headers off
		commandQueue.add(new EchoCommand(0));// echo off
		commandQueue.add(new SelectProtocolCommand(0)); // protocol default
		commandQueue.add(new DescribeProtocolCommand());

		commandQueue.add(new CustomCommand("01 00"));
		commandQueue.add(new CustomCommand("01 20"));
		commandQueue.add(new CustomCommand("01 40"));

		commandQueue.add(new ProtocolCloseCommand()); // quit
		commandQueue.add(new QuitCommand());// end the process

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final Result result = new Result();

		new CommandExecutor(streams, commandQueue, result).start();
	}
}
