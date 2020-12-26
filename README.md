# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the implementation is to provide set of useful function that can be used in Android OBD2 data logger.


//![Alt text](./desk.png?raw=true "Title")


## Pid definitions

Framework uses external JSON files that defines series of supported PID's (SAE J1979) and evaluations formula. 
Default configuration has following structure 

```yaml
{
"formula": "(256 * A + B )/4",
"pid": "0C",
"mode": "01",
"description": "Engine speed",
"unit": "rpm",
"min": "0",
"max": "16,383.75"
}
```





### Example usage, see: IntegrationTest

```java
final CommandsBuffer buffer = new CommandsBuffer();
buffer.add(new ResetCommand());// reset

buffer.add(new ReadVoltagetCommand());
buffer.add(new EchoCommand(0));// echo off

buffer.add(new LineFeedCommand(0)); // line feed off
buffer.add(new HeadersCommand(0));// headers off
buffer.add(new SelectProtocolCommand(0)); // protocol default
buffer.add(new DescribeProtocolCommand());

// 01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c
buffer.add(new SupportedPidsCommand("00")); // get supported pids 41 00 98 3F 80 10

buffer.add(new SupportedPidsCommand("20")); // get supported pids
buffer.add(new SupportedPidsCommand("40")); // get supported pids

buffer.add(new CustomCommand("0C")); // engine rpm
buffer.add(new CustomCommand("0F")); // air intake
buffer.add(new CustomCommand("10")); // maf
buffer.add(new CustomCommand("0B")); // intake manifold pressure
buffer.add(new CustomCommand("0D")); // vehicle speed


buffer.add(new ProtocolCloseCommand()); // protocol close
buffer.add(new QuitCommand());// quite the CommandExecutor

final String obdDongleId = "AABBCC112233";
final Streams streams = StreamFactory.bluetooth(obdDongleId);

final DataCollector collector = new DataCollector();
final ExecutorPolicy executorPolicy  = ExecutorPolicy.builder().frequency(100).build();

final CommandExecutor executor = CommandExecutor
		.builder()
		.streams(streams)
		.buffer(buffer)
		.subscribe(collector)
		.policy(executorPolicy)
		.build();

final ExecutorService executorService = Executors.newFixedThreadPool(1);
executorService.invokeAll(Arrays.asList(executor));

final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();

data.entries().stream().forEach(k -> {
	log.info("{}", k);
});

Assertions.assertThat(collector.getData().containsKey(new SupportedPidsCommand("00")));
executorService.shutdown()


```


### Bellow is an example output from the the typical test run


```java
[main] INFO org.openobd2.core.streams.StreamFactory - Opening connection to bluetooth device: AABBCC112233
BlueCove version 2.1.1-SNAPSHOT on winsock
[main] INFO org.openobd2.core.streams.StreamFactory - Connection to bluetooth device: AABBCC112233 is opened: com.intel.bluetooth.BluetoothRFCommClientConnection@1165b38
[pool-1-thread-1] INFO org.openobd2.core.CommandExecutor - Starting command executor thread..
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.ResetCommand@2cea22, value=null, raw=?)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.ReadVoltagetCommand@5703e10, value=null, raw=ELM327v1.5)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.EchoCommand@57008a9, value=null, raw=ATE0OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.LineFeedCommand@57022f0, value=null, raw=OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.HeadersCommand@57013ec, value=null, raw=OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.SelectProtocolCommand@a897df29, value=null, raw=OK)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.DescribeProtocolCommand@57008c8, value=null, raw=AUTO)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@4704146, mode=01, pid=00), value=[01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c], raw=4100983F8010)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@47048c8, mode=01, pid=20), value=null, raw=NODATA)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@470504a, mode=01, pid=40), value=null, raw=NODATA)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704393, mode=01, pid=0C), value=null, raw=410C0000)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@47043f0, mode=01, pid=0F), value=null, raw=410FAD)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704507, mode=01, pid=10), value=null, raw=41100000)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704374, mode=01, pid=0B), value=null, raw=410B63)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@47043b2, mode=01, pid=0D), value=null, raw=410D00)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[pool-1-thread-1] INFO org.openobd2.core.CommandExecutor - Stopping command executor thread. Finishing communication.
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.ProtocolCloseCommand@5703441, value=null, raw=OK)
[pool-1-thread-1] INFO org.openobd2.core.DeviceIO - Closing streams.
[ForkJoinPool.commonPool-worker-3] INFO org.openobd2.core.DataCollector - Receive data: CommandReply(command=org.openobd2.core.command.QuitCommand@6520878, value=null, raw=null)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.ReadVoltagetCommand@5703e10=CommandReply(command=org.openobd2.core.command.ReadVoltagetCommand@5703e10, value=null, raw=ELM327v1.5)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.LineFeedCommand@57022f0=CommandReply(command=org.openobd2.core.command.LineFeedCommand@57022f0, value=null, raw=OK)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.CustomCommand@47043f0, mode=01, pid=0F)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@47043f0, mode=01, pid=0F), value=null, raw=410FAD)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.CustomCommand@47043b2, mode=01, pid=0D)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@47043b2, mode=01, pid=0D), value=null, raw=410D00)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.CustomCommand@4704393, mode=01, pid=0C)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704393, mode=01, pid=0C), value=null, raw=410C0000)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.CustomCommand@4704374, mode=01, pid=0B)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704374, mode=01, pid=0B), value=null, raw=410B63)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.QuitCommand@6520878=CommandReply(command=org.openobd2.core.command.QuitCommand@6520878, value=null, raw=null)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.ResetCommand@2cea22=CommandReply(command=org.openobd2.core.command.ResetCommand@2cea22, value=null, raw=?)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.EngineTempCommand@47041e1, mode=01, pid=05), value=-6, raw=410522)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.ProtocolCloseCommand@5703441=CommandReply(command=org.openobd2.core.command.ProtocolCloseCommand@5703441, value=null, raw=OK)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@4704146, mode=01, pid=00)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@4704146, mode=01, pid=00), value=[01, 04, 05, 0b, 0c, 0d, 0e, 0f, 10, 11, 1c], raw=4100983F8010)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.CustomCommand@4704507, mode=01, pid=10)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.CustomCommand@4704507, mode=01, pid=10), value=null, raw=41100000)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.DescribeProtocolCommand@57008c8=CommandReply(command=org.openobd2.core.command.DescribeProtocolCommand@57008c8, value=null, raw=AUTO)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@47048c8, mode=01, pid=20)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@47048c8, mode=01, pid=20), value=null, raw=NODATA)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.EchoCommand@57008a9=CommandReply(command=org.openobd2.core.command.EchoCommand@57008a9, value=null, raw=ATE0OK)
[main] INFO org.openobd2.core.IntegrationTest - ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@470504a, mode=01, pid=40)=CommandReply(command=ObdFrame(super=org.openobd2.core.command.SupportedPidsCommand@470504a, mode=01, pid=40), value=null, raw=NODATA)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.HeadersCommand@57013ec=CommandReply(command=org.openobd2.core.command.HeadersCommand@57013ec, value=null, raw=OK)
[main] INFO org.openobd2.core.IntegrationTest - org.openobd2.core.command.SelectProtocolCommand@a897df29=CommandReply(command=org.openobd2.core.command.SelectProtocolCommand@a897df29, value=null, raw=OK)
BlueCove stack shutdown completed

```

# Architecture drivers

1. Performance
2. Reliability
3. 


# What is not done yet

1. Error handling
2. 


# Design
1. TODO


# TODO
1. 