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

commands.add(new QueryForPidsCommand("00")); // get supported pids 41 00 98 3F 80 10
commands.add(new QueryForPidsCommand("20")); // get supported pids
commands.add(new QueryForPidsCommand("40")); // get supported pids
commands.add(new CustomCommand("01 0C")); // engine rpm
commands.add(new CustomCommand("01 0F")); // air intake
commands.add(new CustomCommand("01 10")); // maf
commands.add(new CustomCommand("01 0B")); // intake manifold pressure
commands.add(new CustomCommand("01 0D")); // vehicle speed

commands.add(new ProtocolCloseCommand()); // quit
commands.add(new QuitCommand());// end the process

final String obdDongleId = "AABBCC112233";
final Streams streams = StreamFactory.bt(obdDongleId);

final CommandReplySubscriber obdSubscriber = new CommandReplySubscriber();

final CommandExecutor commandExecutor = CommandExecutor.builder().streams(streams).commands(commands)
		.subscriber(obdSubscriber).build();

commandExecutor.start();

```


### Bellow is an example output from the the typical test run


```
[main] INFO org.openelm327.core.streams.StreamFactory - Opening connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openelm327.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@2286778
[Thread-0] INFO org.openelm327.core.CommandExecutor - Starting command executor thread..
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATZ), values=[], raw=?)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATRV), values=[], raw=ELM327 v1.5)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=AT L0), values=[], raw=AT L0OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATH0), values=[], raw=ATH0OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATE0), values=[], raw=ATE0OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=AT SP0), values=[], raw=OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATDP), values=[], raw=AUTO)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 00), values=[01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c], raw=41 00 98 3F 80 10)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 20), values=[], raw=NO DATA)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 40), values=[], raw=NO DATA)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 0C), values=[], raw=41 0C 00 00)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 0F), values=[], raw=41 0F AD)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 10), values=[], raw=41 10 00 00)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 0B), values=[], raw=41 0B 63)
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=01 0D), values=[], raw=41 0D 00)
[Thread-0] INFO org.openelm327.core.CommandExecutor - Stopping command executor thread. Finishing communication.
[Thread-0] INFO org.openelm327.core.IOManager - Closing streams.
[ForkJoinPool.commonPool-worker-3] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandReply(command=Command(query=ATPC), values=[], raw=OK)
BlueCove stack shutdown completed

```


# What is not done yet

1. Error handling
2. 

