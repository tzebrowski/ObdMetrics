# Yet another OBD2 client library

## About

This is another java library that simplifies communication with OBD2 dongle like ELM327 clones


### Example usage

```

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

commandQueue.add(new ProtocolCloseCommand()); //quit
commandQueue.add(new QuitCommand());// end the process

final String obdDongleId = "AABBCC112233";
final Streams streams = StreamFactory.bt(obdDongleId);
new CommandExecutor(streams, commandQueue).start();

```




### Bellow Example output from the the .TestController


```

[main] INFO org.openelm327.core.streams.StreamFactory - Openning connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openelm327.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@6fc6f14e
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATZ
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATZ. Recieved data: ?, length: 1
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATRV
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATRV. Recieved data: ELM327v1.5, length: 10
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: AT L0
[Thread-0] INFO org.openelm327.core.IOManager - Command: AT L0. Recieved data: ATL0OK, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATH0
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATH0. Recieved data: ATH0OK, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATE0
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATE0. Recieved data: ATE0OK, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: AT SP0
[Thread-0] INFO org.openelm327.core.IOManager - Command: AT SP0. Recieved data: OK, length: 2
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATDP
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATDP. Recieved data: AUTO, length: 4
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 00
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 00. Recieved data: SEARCHING...4100983F8010, length: 24
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 20
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 20. Recieved data: NODATA, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: 01 40
[Thread-0] INFO org.openelm327.core.IOManager - Command: 01 40. Recieved data: NODATA, length: 6
[Thread-0] INFO org.openelm327.core.IOManager - Sending command: ATPC
[Thread-0] INFO org.openelm327.core.IOManager - Command: ATPC. Recieved data: OK, length: 2
[Thread-0] INFO org.openelm327.core.CommandExecutor - Closing streams. Finishing communication.
BlueCove stack shutdown completed

```