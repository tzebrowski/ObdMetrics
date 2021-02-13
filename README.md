# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the framework is to provide set of useful features that allows to collect and process vehicle metrics.
Example usage can be found under: [Android OBD2 data logger](https://github.com/tzebrowski/AlfaDataLogger "AlfaDataLogger") 


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
* Generic list of PIDs can be found [here](./src/main/resources/mode01.json "mode01.json")



### Batch commands

Framework allows to ask for up to 6 PID's in a single request.

*Request:*

``` 
01 01 03 04 05 06 07
```

*Response:*

``` 
0110:4101000771611:0300000400051c2:06800781000000
```


### Multiple decoders for same PID

You can add multiple decoders for single PID. In the example bellow there 2 decoders for PID 0115. 
One that calculates AFR, and second one shows Oxygen sensor voltage.

```

	{
		"id": "22",
		"mode": "01",
		"pid": 15,
		"length": 2,
		"description": "Calculated AFR",
		"min": "0",
		"max": "20",
		"units": "Volts %",
		"formula": "parseFloat( ((0.680413+((0.00488*(A / 200))*0.201356))*14.7).toFixed(2) )"
	},
	{
		"id": "23",
		"mode": "01",
		"pid": 15,
		"length": 2,
		"description": "Oxygen sensor voltage",
		"min": "0",
		"max": "5",
		"units": "Volts %",
		"formula": "parseFloat(A / 200)"
	},


```



### Support for 22 mode

* It has support for mode 22 PIDS
* Configuration: [alfa.json](./src/main/resources/alfa.json?raw=true "alfa.json")
* Integration test: [AlfaIntegrationTest](./src/test/java/org/obd/metrics/integration/AlfaIntegrationTest.java "AlfaIntegrationTest.java") 


### Formula calculation

* Framework is able to calculate equation defined within Pid's definition to get PID value. 
It may include additional JavaScript functions like *Math.floor* ..

``` 
Math.floor(((A*256)+B)/32768((C*256)+D)/8192)
```



## Supported devices

* ELM 1.5
* ELM 2.2


## Verified against 

So far FW has been verified against following ECU
* MED 17.3.1
* MED 17.5.5
* EDC 15.x


## Design view

###  Component view


![Alt text](./src/main/resources/component.png?raw=true "Component view")


###  Model view


![Alt text](./src/main/resources/model.png?raw=true "Model view")


###  API



#### Usage


##### 

Workflow API, details:  [WorkflowTest](./src/test/java/org/obd/metrics/integration/PerformanceTest.java "PerformanceTest.java")

```java

final Connection connection = openConnection();
final DataCollector collector = new DataCollector();

final Workflow workflow = Workflow.mode1().equationEngine("JavaScript").subscriber(collector).batchEnabled(true).build();
workflow.start(connection);

final Callable<String> end = () -> {

	Thread.sleep(15000);
	log.info("Ending the process of collecting the data");
	workflow.stop();
	return "end";
};

final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
newFixedThreadPool.invokeAll(Arrays.asList(end));

final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();
Assertions.assertThat(data).isNotNull();

newFixedThreadPool.shutdown();
```

Example usage, see: [IntegrationTest](./src/test/java/org/obd/metrics/integration/IntegrationTest.java "IntegrationTest.java") for the details.

```java
final Connection connection = openConnection();
		
final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("generic.json");

final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

final CommandsBuffer buffer = CommandsBuffer.instance(); // Define command buffer
buffer.add(Mode1CommandGroup.INIT_PROTO_DEFAULT); // Add protocol initialization AT commands
buffer.add(Mode1CommandGroup.SUPPORTED_PIDS); // Request for supported PID's

// Read signals from the device
final ObdCommand intakeAirTempCommand = new ObdCommand(pidRegistry.findBy("01", "0F"));// Intake air temperature
buffer.add(intakeAirTempCommand)
	.add(new ObdCommand(pidRegistry.findBy("01", "0C"))) // Engine rpm
	.add(new ObdCommand(pidRegistry.findBy("01", "10"))) // Maf
	.add(new ObdCommand(pidRegistry.findBy("01", "0B"))) // Intake manifold pressure
	.add(new ObdCommand(pidRegistry.findBy("01", "0D"))) // Behicle speed
	.add(new ObdCommand(pidRegistry.findBy("01", "05"))) // Engine temp
	.add(new QuitCommand());// Last command that will close the communication

final DataCollector collector = new DataCollector(); // It collects the

final CodecRegistry codecRegistry = CodecRegistry.builder().evaluateEngine("JavaScript").pids(pidRegistry)
		.build();

final CommandExecutor executor = CommandExecutor.builder().connection(connection).buffer(buffer).subscribe(collector)
		.policy(ExecutorPolicy.builder().frequency(100).build()).codecRegistry(codecRegistry).build();

final ExecutorService executorService = Executors.newFixedThreadPool(1);
executorService.invokeAll(Arrays.asList(executor));

final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();
Assertions.assertThat(data.containsKey(intakeAirTempCommand));

final Collection<CommandReply<?>> collection = data.get(intakeAirTempCommand);
Assertions.assertThat(collection.iterator().hasNext()).isTrue();

// 133 ??
Assertions.assertThat(collection.iterator().next().getValue()).isEqualTo(133.0);

executorService.shutdown();
source.close();
```

# Architecture drivers

1. Extensionality
2. Reliability


