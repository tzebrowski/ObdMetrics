# Yet another Java OBD2 client library

## About

This is another java library that is intended to simplify communication with OBD2 dongles like ELM327 clones.
The goal of the implementation is to provide set of useful function that can be used in Android OBD2 data logger.


### Example usage, see: IntegrationTest

```
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

```


### Bellow is an example output from the the typical test run


```
[main] INFO org.openelm327.core.streams.StreamFactory - Opening connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openelm327.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@527740a2
[pool-1-thread-1] INFO org.openelm327.core.CommandExecutor - Starting command executor thread..
[pool-1-thread-1] INFO org.openelm327.core.CommandExecutor - Stopping command executor thread. Finishing communication.
[pool-1-thread-1] INFO org.openelm327.core.IOManager - Closing streams.
[main] INFO org.openelm327.core.IntegrationTest - Result of command executor: 
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 00, type=QueryForPidsCommand)=CommandReply(command=Command(query=01 00, type=QueryForPidsCommand), value=[01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c], raw=41 00 98 3F 80 10, timestamp=1608633951485)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATZ, type=ResetCommand)=CommandReply(command=Command(query=ATZ, type=ResetCommand), value=null, raw=?, timestamp=1608633945345)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 10, type=CustomCommand)=CommandReply(command=Command(query=01 10, type=CustomCommand), value=null, raw=41 10 00 00, timestamp=1608633953216)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 20, type=QueryForPidsCommand)=CommandReply(command=Command(query=01 20, type=QueryForPidsCommand), value=null, raw=NO DATA, timestamp=1608633951797)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATDP, type=DescribeProtocolCommand)=CommandReply(command=Command(query=ATDP, type=DescribeProtocolCommand), value=null, raw=AUTO, timestamp=1608633946216)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 05, type=EngineTempCommand)=CommandReply(command=Command(query=01 05, type=EngineTempCommand), value=-6, raw=41 05 22, timestamp=1608633954114)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 05, type=EngineTempCommand)=CommandReply(command=Command(query=01 05, type=EngineTempCommand), value=-6, raw=41 05 22, timestamp=1608633954417)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 05, type=EngineTempCommand)=CommandReply(command=Command(query=01 05, type=EngineTempCommand), value=-6, raw=41 05 22, timestamp=1608633954713)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATE0, type=EchoCommand)=CommandReply(command=Command(query=ATE0, type=EchoCommand), value=null, raw=ATE0OK, timestamp=1608633946191)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATH0, type=HeadersCommand)=CommandReply(command=Command(query=ATH0, type=HeadersCommand), value=null, raw=ATH0OK, timestamp=1608633946178)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 0F, type=CustomCommand)=CommandReply(command=Command(query=01 0F, type=CustomCommand), value=null, raw=41 0F AD, timestamp=1608633952850)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATRV, type=ReadVoltagetCommand)=CommandReply(command=Command(query=ATRV, type=ReadVoltagetCommand), value=null, raw=ELM327 v1.5, timestamp=1608633946153)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 40, type=QueryForPidsCommand)=CommandReply(command=Command(query=01 40, type=QueryForPidsCommand), value=null, raw=NO DATA, timestamp=1608633952104)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 0D, type=CustomCommand)=CommandReply(command=Command(query=01 0D, type=CustomCommand), value=null, raw=41 0D 00, timestamp=1608633953817)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=AT L0, type=CustomCommand)=CommandReply(command=Command(query=AT L0, type=CustomCommand), value=null, raw=AT L0OK, timestamp=1608633946165)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 0C, type=CustomCommand)=CommandReply(command=Command(query=01 0C, type=CustomCommand), value=null, raw=41 0C 00 00, timestamp=1608633952473)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=01 0B, type=CustomCommand)=CommandReply(command=Command(query=01 0B, type=CustomCommand), value=null, raw=41 0B 63, timestamp=1608633953517)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=AT SP0, type=SelectProtocolCommand)=CommandReply(command=Command(query=AT SP0, type=SelectProtocolCommand), value=null, raw=OK, timestamp=1608633946203)
[main] INFO org.openelm327.core.IntegrationTest - Command(query=ATPC, type=ProtocolCloseCommand)=CommandReply(command=Command(query=ATPC, type=ProtocolCloseCommand), value=null, raw=OK, timestamp=1608633954727)
BlueCove stack shutdown completed




```


# What is not done yet

1. Error handling
2. 

