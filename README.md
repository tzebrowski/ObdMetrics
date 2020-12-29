# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the implementation is to provide set of useful function that can be used in Android OBD2 data logger.

## What makes this framework unique ?

### Pid definitions

* Framework uses external JSON files that defines series of supported PID's (SAE J1979) and evaluations formula. Default configuration has following structure 

```yaml
{
 "mode": "01",
 "pid": 23,
 "length": 2,
 "description": "Fuel Rail Pressure (diesel)",
 "min": 0,
 "max": 655350,
 "units": "kPa (gauge)",
 "formula": "((A*256)+B) * 10"
},
```


* Framework is able to work with multiple sources of PID's that are specified for different automotive manufacturers.
* Generic list of PIDs can be found [here](./src/main/resources/generic.json?raw=true "generic.json")


### Support for 22 mode

* It has support for mode 22 PIDS
* Configuration: [alfa.json](./src/main/resources/alfa.json?raw=true "alfa.json")
* Integration test: [AlfaIntegrationTest](./src/test/java/org/openobd2/core/AlfaIntegrationTest.java?raw=true "AlfaIntegrationTest.java") 



### Formula calculation

* Framework is able to calculate formulas defined within Pid's definition to get PID value. 

``` 
((A*256)+B)/32768((C*256)+D)/8192
```


## Design view

![Alt text](./src/main/resources/design.png?raw=true "design view")



###  API



#### Example usage

Example usage, see: [IntegrationTest](./src/test/java/org/openobd2/core/IntegrationTest.java?raw=true "IntegrationTest.java") 

```java
		final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

		
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

		buffer.add(new CustomCommand(pidRegistry.findBy("01","0C"))); // engine rpm
		buffer.add(new CustomCommand(pidRegistry.findBy("01","0F"))); // air intake
		buffer.add(new CustomCommand(pidRegistry.findBy("01","10"))); // maf
		buffer.add(new CustomCommand(pidRegistry.findBy("01","0B"))); // intake manifold pressure
		buffer.add(new CustomCommand(pidRegistry.findBy("01","0D"))); // vehicle speed


		buffer.add(new ProtocolCloseCommand()); // protocol close
		buffer.add(new QuitCommand());// quite the CommandExecutor

		final Streams streams = BluetoothStream.builder().adapter("AABBCC112233").build();

		final DataCollector collector = new DataCollector();
		
		
		final ConverterRegistry converterRegistry = ConverterRegistry.builder().pidRegistry(pidRegistry).build();
		
		final CommandExecutor executor = CommandExecutor
				.builder()
				.streams(streams)
				.buffer(buffer)
				.subscribe(collector)
				.policy(ExecutorPolicy.builder().frequency(100).build())
				.converterRegistry(converterRegistry)
				.build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));
```

# Architecture drivers

1. Performance
2. Reliability
3. ...


# What is not done yet

1. Error handling
2. ...

