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
* Generic list of PIDs can be found [here](./src/main/resources/generic.json "generic.json")


### Support for 22 mode

* It has support for mode 22 PIDS
* Configuration: [alfa.json](./src/main/resources/alfa.json?raw=true "alfa.json")
* Integration test: [AlfaIntegrationTest](./src/test/java/org/openobd2/core/AlfaIntegrationTest.java "AlfaIntegrationTest.java") 



### Formula calculation

* Framework is able to calculate equation defined within Pid's definition to get PID value. 
It may include additional JavaScript functions like *Math.floor* ..

``` 
Math.floor(((A*256)+B)/32768((C*256)+D)/8192)
```


## Design view

###  Component view


![Alt text](./src/main/resources/component.png?raw=true "Component view")


###  Model view


![Alt text](./src/main/resources/model.png?raw=true "Model view")


###  API



#### Usage

Example usage, see: [IntegrationTest](./src/test/java/org/openobd2/core/IntegrationTest.java "IntegrationTest.java") for the details.

```java
final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("generic.json");

final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

final CommandsBuffer buffer = new CommandsBuffer(); //Define command buffer
buffer.add(CommandSet.INIT_PROTO_DEFAULT); // Add protocol initialization AT commands
buffer.add(CommandSet.MODE1_SUPPORTED_PIDS); // Request for supported PID's

//Read signals from the device
buffer.add(new ObdCommand(pidRegistry.findBy("01", "0C"))); // Engine rpm
buffer.add(new ObdCommand(pidRegistry.findBy("01", "0F"))); // Air intake
buffer.add(new ObdCommand(pidRegistry.findBy("01", "10"))); // Maf
buffer.add(new ObdCommand(pidRegistry.findBy("01", "0B"))); // Intake manifold pressure
buffer.add(new ObdCommand(pidRegistry.findBy("01", "0D"))); // Behicle speed
buffer.add(new ObdCommand(pidRegistry.findBy("01", "05"))); // Engine temp

buffer.add(new QuitCommand());// Last command that will close the communication

final Channel channel = BluetoothChannel.builder().adapter("AABBCC112233").build(); // Define BT streams

final DataCollector collector = new DataCollector(); //It collects the 

final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();

final CommandExecutor executor = CommandExecutor
		.builder()
		.streams(channel)
		.buffer(buffer)
		.subscribe(collector)
		.policy(ExecutorPolicy.builder().frequency(100).build())
		.codecRegistry(codecRegistry)
		.build();

final ExecutorService executorService = Executors.newFixedThreadPool(1);
executorService.invokeAll(Arrays.asList(executor));

final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();
```

# Architecture drivers

1. Extensionality
2. Reliability
3. ...


