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
[main] INFO org.openelm327.core.streams.StreamFactory - Openning connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openelm327.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@6fc6f14e
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATZ
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATZ. Recieved data: ?, length: 1
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATRV
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATRV. Recieved data: ELM327 v1.5, length: 11
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: AT L0
[Thread-0] INFO org.openelm327.core.IOManager - Command: AT L0. Recieved data: AT L0OK, length: 7
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATH0
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATH0. Recieved data: ATH0OK, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATE0
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATE0. Recieved data: ATE0OK, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: AT SP0
[Thread-0] INFO org.openelm327.core.IOManager - Command: AT SP0. Recieved data: OK, length: 2
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATDP
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATDP. Recieved data: AUTO, length: 4
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 00
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 00. Recieved data: SEARCHING...41 00 98 3F 80 10, length: 29
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 20
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 20. Recieved data: NO DATA, length: 7
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 40
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 40. Recieved data: NO DATA, length: 7
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATPC
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATPC. Recieved data: OK, length: 2
[Thread-0] INFO org.openelm327.core.CommandExecutor - Closing streams. Finishing communication.
BlueCove stack shutdown completed

```