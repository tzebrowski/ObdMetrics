# Yet another OBD2 client library

## About

This is another java library that simplifies communication with OBD2 dongle like ELM327 clones


### Example usage

```
final Commands commands = new Commands();
commands.add(new ResetCommand());// reset
commands.add(new ReadVoltagetCommand());
commands.add(new CustomCommand("AT L0"));
commands.add(new HeadersCommand(0));// headers off
commands.add(new EchoCommand(0));// echo off
commands.add(new SelectProtocolCommand(0)); // protocol default
commands.add(new DescribeProtocolCommand());

commands.add(new CustomCommand("01 00"));
commands.add(new CustomCommand("01 20"));
commands.add(new CustomCommand("01 40"));

commands.add(new ProtocolCloseCommand()); // quit
commands.add(new QuitCommand());// end the process

final String obdDongleId = "AABBCC112233";
final Streams streams = StreamFactory.bt(obdDongleId);

final Result result = new Result();

final CommandExecutor commandExecutor = CommandExecutor.builder().streams(streams).commands(commands)
		.result(result).build();

commandExecutor.start();

```




### Bellow is an example output from the the test run


```
[main] INFO org.openelm327.core.streams.StreamFactory - Opening connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openelm327.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@6fc6f14e
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATZ), raw=?)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATRV), raw=ELM327 v1.5)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=AT L0), raw=AT L0OK)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATH0), raw=ATH0OK)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATE0), raw=ATE0OK)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=AT SP0), raw=OK)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATDP), raw=AUTO)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=01 00), raw=41 00 98 3F 80 10)
[Thread-0] INFO org.openelm327.core.CommandResultSubscriber - Receive command result: CommandResult(command=Command(query=ATPC), raw=OK)
BlueCove stack shutdown completed
[Thread-0] INFO org.openelm327.core.CommandExecutor - Closing streams. Finishing communication.



```