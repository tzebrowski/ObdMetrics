# OBD Metrics

![CI](https://github.com/tzebrowski/ObdMetrics/workflows/Deploy/badge.svg?branch=main) 
![codecov](https://codecov.io/gh/tzebrowski/ObdMetrics/branch/main/graph/badge.svg)
![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.tzebrowski/obd-metrics/badge.svg)


## About

`OBD Metrics` is a Java OBD2 framework that is intended to simplify communication with OBD2 adapters like `ELM327`/`STNxxx` clones.</br>
The goal behind the implementation is to provide the extensionable framework which covers selected aspects of communication with the OBD2 adapters like reading OBD telemetry data and can be a foundation for the future OBD2 oriented applications. 


![Alt text](./src/main/resources/highlevel.jpg?raw=true "Big Picture")

### Supported use-cases:
* Collecting vehicle telemetry data (Metrics)
* Reading Vehicle Metadata, e.g: VIN
* Reading Diagnostic Trouble Codes (DTC)

### Supported adapters and protocols
- The framework supports `ELM327` based adapters
	- The framework is compatible with the `ELM327` AT command set
- The framework supports`STNxxxx` based adapters.  More here: https://www.scantool.net/
	- The framework is able to utilize `ST` command set available in the `STNxxxx` device family. More here: https://www.scantool.net/

### Example usage of the framework:

* [ObdGraphs](https://github.com/tzebrowski/ObdGraphs "ObdGraphs")   
* [OBD Metrics Demo](https://github.com/tzebrowski/ObdMetricsDemo "ObdMetricsDemo") 

## What makes this framework unique ?

#### OBD2 PIDs/Sensors defined as configuration

[OBD2 PIDs](https://en.wikipedia.org/wiki/OBD-II_PIDs "OBD2 PIDs") hereinafter referred to as `PIDs` or `OBD2 PIDs`  processed by the framework are defined in the external `resource files` and are described by the JSON schema.</br> 
Through this design decision PIDs does not need to be necessarily part of the framework and might be supplied by external party.</br>
Within single `resource file` PIDs are divided into distinct groups, following categories are available:
- `capabilities` - Supported PIDs category  
- `dtc` - Diagnostic trouble code category
- `metadata` - Metadata PIDs category. PIDs which are read just once during session with the Adapter
- `livedata` - Livedata PIDs category. PIDs which are read frequently during session with the Adapter
- `routine` - Routines PIDs category. PIDs which are executed on demand.

During single session the framework is able to work with multiple `resource files` which might be specific for different automotive manufacturers.</br>
Generic list of PIDs can be found [here](./src/main/resources/mode01.json "mode01.json")

Configuration might looks like the one below example.

```json
{	
	"capabilities": [
		{
			"id": "21000",
			"mode": "01",
			"pid": "00",
			"description": "Supported PIDs 00"
		}
	],
	"dtc": [
		{
			"id": "27000",
			"mode": "19",
			"pid": "020D",
			"description": "DTC Read",
			"successCode": "5902CF",
			"commandClass": "org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand"
		}
	],
	"metadata": [
		{
			
			"id": "17001",
			"mode": "22",
			"pid": "F190",
			"description": "Vehicle Identification Number"
		},
	],
	"livedata": [
		{
			"priority": 0,
			"id": "7001",
			"mode": "22",
			"pid": "195A",
			"length": 2,
			"description": "Boost\nPressure",
			"min": 0,
			"max": 2800,
			"units": "mbar",
			"formula": "(A*256+B)"
		},
	],
	
	"routine": [
		{
			"id": "10002",
			"mode": "2F",
			"pid": "55720308",
			"description":"Turn dashboard illumination on",
			"overrides" : {
				"canMode": "INSTRUMENT_PANEL"
			}
		}
	],	
}
```


#### Dynamic formula calculation

The framework is able to calculate PID's value from the RAW data using dynamic formulas written in `JavaScipt`.  
The formula can include additional `JavaScript` functions like *Math.floor* .
This feature dramatically decrease time to delivering new PIDs and reduces need to write dedicated java based decoders.


Example for *Measured Boost Pressure* PID

```json 
{ 
	"pid": "195A",
	"length": 2,
	"formula": "(A*256+B) | 0",
}
```

Given that `62195A09AA` hex data is received from the ECU for above PID, FW implementation converts it (splitting by two characters) into decimal numbers identified by two parameters `A` and `B` (PID length here is equal 2).
Received data `62195A 09AA` is later passed to the formula as follows:

* `A` = `09` = `9` 
* `B` = `AA` = `170`

Finally this results as `9 * 256 + 170 = 2474`. The value `2474` is what FW emits for later processing.


#### Signed HEX numbers 

By default framework interprets all `hex` as unsigned numbers. 
In order to process negative numbers, property `signed=true` must be set `true` within the PID definition. 
This property tells framework to decoded hex value using special rules. 
Moreover, calculation formula must contains dedicated statement: `if (typeof X === 'undefined')...` to handle negative number which might be received under `X` parameter, see example bellow:

*Definition*
  
```json  
{
	"description": "Measured Intake\nValve Crossing",
	"signed": true,
	"formula": "if (typeof X === 'undefined') X =(A*256+B); parseFloat((X * 0.0078125).toFixed(3))"
},

```


#### Formula external parameters

Framework allows to pass external parameters into PID formula. Through this calculation formula can be modified dynamically based on external factors.
One of the example is calculation of the fuel level based on tank size, which might have different size in different vehicles. 

In this example `unit_tank_size` is passed as the external parameter.
  
```json  
{
	"priority": 3,
	"id": "7040",
	"mode": "22",
	"pid": "1001",
	"length": 1,
	"description": "Fuel Level\n(Liters)",
	"min": "0",
	"max": "100",
	"units": "L",
	"formula": "parseFloat(((A*0.3921568)/100 * unit_tank_size).toFixed(1))"	
},
```

```java
final Adjustments optional = Adjustments.builder()
		.formuilaExternalParams(FormulaExternalParams.builder().param("unit_tank_size",tankSize).build())
		.build();
```

#### Querying multiple ECUs within the same communication session

The framework is able to speak with multiple ECU with the same communication session. 
Once sessions established `ObdMetrics` queries different modules like TCU, ECU without additional overhead. 
Moreover FW it's able to work either with CAN 11 bit or CAN 29 bit headers.

```java

final Pids pids = Pids
        .builder()
        .resource(contextClassLoader.getResource("mode01.json"))
        .resource(contextClassLoader).getResource("alfa.json")).build();
        .build();

final Init init = Init.builder()
        .delay(1000)
        .header(Header.builder().mode("22").header("DA10F1").build())
        .header(Header.builder().mode("01").header("DB33F1").build())
        .protocol(Protocol.CAN_29)
        .sequence(DefaultCommandGroup.INIT).build();

final Workflow workflow = Workflow
        .pids(pids)
workflow.start(BluetoothConnection.openConnection(), query, init, optional);
```


##### CAN headers overrides 

The framework allows to override CAN headers just for specific PID's, and adjust it at runtime.

```json
{
	"priority": 0,
	"id": "7029",
	"mode": "22",
	"pid": "051A",
	"length": 1,
	"description": "Gear Engaged",
	"min": "-1",
	"max": "10",
	"units": "",
	"type": "INT",
	"formula": "x=A; if (x==221) {x=0 } else if (x==238) {x=-1} else { x=A/17} x",
	"overrides" : {
		"canMode": "555",
		"batchEnabled": false
	}
},

```

```java
final Init init = Init.builder()
  .header(Header.builder().mode("22").header("DA10F1").build())
  .header(Header.builder().mode("01").header("DB33F1").build())
   //overrides CAN mode
  .header(Header.builder().mode("555").header("DA18F1").build()) 
  .protocol(Protocol.CAN_29)
  .build();
```


#### Testability

As part of the solution there is available dedicated module name [ObdMetricsTest](https://github.com/tzebrowski/ObdMetricsTest/tree/main "ObdMetricsTest") which exposes set of interfaces like: `CodecTest` which allows to write clean PIDs tests with the focus on business aspects of its development.

```java

interface MultiJet_2_2_Test extends CodecTest {

	final String RESOURCE_NAME = "giulia_2_2_multijet.json";

	@Override
	default String getPidFile() {
		return RESOURCE_NAME;
	}
}

public class AirTempMafTest implements MultiJet_2_2_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62193F0000=-40",
			"62193F1100=47",
	}, delimiter = '=')
	public void test(String input, Integer expected) {
		assertEquals(input, expected);
	}
}
```



#### Custom decoders

The framework allows to provide own custom PIDs decoders, examples: 

* [VIN decoder](./src/main/java/org/obd/metrics/command/meta/HexCommand.java "HexCommand.java") for `0902` query.
* [Supported PIDS decoder](./src/main/java/org/obd/metrics/command/SupportedPidsCommand.java "SupportedPidsCommand.java") for `01 00, 01 20,01 40, ...` query.



#### Diagnostics interface

The framework collects metadata about commands processing, you can easily get information about *max*, *min*, *mean*, value for the current session with ECU.

<details>
<summary>Example</summary>
<p>

```java
final Workflow workflow = Workflow
        .instance()
        .pids(pids)
        .initialize();

workflow.start(connection, query, init, optional);

final PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
final PidDefinition rpm = pidRegistry.findBy(13l);
final Diagnostics diagnostics = workflow.getDiagnostics();
final Histogram rpmHist = diagnostics.histogram().findBy(rpm);
Assertions.assertThat(rpmHist.getMin()).isGreaterThan(500);
```

</p>
</details> 

#### Multiple decoders for the single PID

You can add multiple decoders for single PID. In the example bellow there are 2 decoders for PID 0115. 
One that calculates AFR, and second one shows Oxygen sensor voltage.

```json
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
}

```


#### Performance optimization

##### Number of lines adapter should return

The framework is able to calculate number of lines Adapter should return for the given query. This optimization speedup communication with the ECU.

*Request:*

``` 
01 0C 0B 11 0D 04 06 3
```

Last digit in the query: `3`  indicates that Adapter should back to the caller as soon as it gets 3 lines from the ECU.


##### Batch commands

The framework supports `batch` queries and allows to query for up to 6 PID's in a single request for the `mode 01`. 
For the `mode 22` its allowed to query up to 11 PID's in the single call.

###### Example for `Mode 01`
*Request:*

``` 
01 01 03 04 05 06 07
```

*Response:*

``` 
0110:4101000771611:0300000400051c2:06800781000000
```

##### Priority commands

It's possible to set priority for some of the PID's so they are pulled from the Adapter more frequently than others. 
Intention of this feature is to get more accurate result for `dynamic` PID's.
A good example here, is a `RPM` or `Boost pressure` PID's that should be queried more often because of their characteristics over the time than `Engine Coolant Temperature` has (less frequent changes).


#### Mocking OBD Adapter connection

There is not necessary to have physical ECU device to play with the framework. 
In the pre-integration tests where the FW API is verified its possible to use `MockAdapterConnection` that simulates behavior of the real OBD adapter.

```java
MockAdapterConnection connection = MockAdapterConnection.builder()
		.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
		.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
		.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
		.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
		.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
		.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
       .build();
```


##  Framework 

The framework consists of multiple components that are intended to exchange the messages with the Adapter (Request-Response model) and propagate decoded metrics to the target application using a non-blocking manner (Pub-Sub model). All the internal details like managing multiple threads are hidden and the target application that includes FW must provide just a few interfaces that are required for establishing the connection with the Adapter and receiving the OBD metrics.

 
API of the framework is exposed through `Workflow` interface which centralize all features in the single place, see: [Workflow](./src/main/java/org/obd/metrics/api/Workflow.java "Workflow.java").
Particular workflow implementations can be instantiated by calling `Workflow.instance().initialize()`


<details>
<summary>Workflow interface</summary>
<p>


```java

/**
 * {@link Workflow} is the main interface that expose the API of the framework.
 * It contains typical operations that allows to play with the OBD adapters
 * like:
 * <ul>
 * <li>Connecting to the Adapter</li>
 * <li>Disconnecting from the Adapter</li>
 * <li>Collecting OBD2 metrics</li>
 * <li>Obtaining statistics registry</li>
 * <li>Obtaining OBD2 PIDs/Sensor registry</li>
 * <li>Gets notifications about errors that appears during interaction with the
 * device.</li>
 * 
 * </ul>
 * 
 * @version 9.2.0
 * @see Adjustments
 * @see AdapterConnection
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {


	/**
	 * Execute routine for already running workflow
	 * 
	 * @param id   id of routine
	 * @param init init settings of the Adapter
	 */
	WorkflowExecutionStatus executeRoutine(@NonNull Long id, @NonNull Init init);
	
	/**
	 * Updates query for already running workflow
	 * 
	 * @param query       queried PID's (parameter is mandatory)
	 * @param init        init settings of the Adapter
	 * @param adjustments additional settings for process of collection the data
	 */
	WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init, @NonNull Adjustments adjustments);

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection the connection to the Adapter (parameter is mandatory)
	 * @param query      queried PID's (parameter is mandatory)
	 */
	default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query) {
		return start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection  the connection to the Adapter (parameter is mandatory)
	 * @param query       queried PID's (parameter is mandatory)
	 * @param adjustments additional settings for process of collection the data
	 */
	default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
			Adjustments adjustments) {
		return start(connection, query, Init.DEFAULT, adjustments);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param adjustements additional settings for process of collection the data
	 * @param connection   the connection to the Adapter (parameter is mandatory)
	 * @param query        queried PID's (parameter is mandatory)
	 * @param init         init settings of the Adapter
	 */
	WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
			Adjustments adjustements);

	/**
	 * Stops the current workflow.
	 */
	default void stop() {
		stop(true);
	}

	/**
	 * Stops the current workflow.
	 * 
	 * @param gracefulStop indicates whether workflow should be gracefully stopped.
	 */
	void stop(boolean gracefulStop);

	/**
	 * Informs whether {@link Workflow} process is already running.
	 * 
	 * @return true when process is already running.
	 */
	boolean isRunning();

	/**
	 * Rebuild {@link PidDefinitionRegistry} with new resources
	 * 
	 * @param pids new resources
	 */
	void updatePidRegistry(Pids pids);

	/**
	 * Gets the current pid registry for the workflow.
	 * 
	 * @return instance of {@link PidDefinitionRegistry}
	 */
	PidDefinitionRegistry getPidRegistry();

	/**
	 * Gets diagnostics collected during the session.
	 * 
	 * @return instance of {@link Diagnostics}
	 */
	Diagnostics getDiagnostics();

	/**
	 * Gets allerts collected during the session.
	 * 
	 * @return instance of {@link Alerts}
	 */
	Alerts getAlerts();

	/**
	 * It creates default {@link Workflow} implementation.
	 * 
	 * @param pids                   PID's configuration
	 * @param formulaEvaluatorConfig the instance of {@link FormulaEvaluatorConfig}.
	 *                               Might be null.
	 * @param observer               the instance of {@link ReplyObserver}
	 * @param lifecycleList          the instance of {@link Lifecycle}
	 * @return instance of {@link Workflow}
	 */
	@Builder(builderMethodName = "instance", buildMethodName = "initialize")
	static Workflow newInstance(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			@NonNull ReplyObserver<Reply<?>> observer, @Singular("lifecycle") List<Lifecycle> lifecycleList) {

		return new DefaultWorkflow(pids, formulaEvaluatorConfig, observer, lifecycleList);
	}
}

```
</p>
</details> 


## Quality

Quality of the project is ensured by junit and integration tests. 
In order to ensure that coverage is on the right level since 0.0.3-SNAPTHOST jacoco check plugin is part of the build.
Minimum coverage ratio is set to 80%, build fails if not meet.
 



## Verified against 

Framework has been verified against following ECU.

* Marelli MM10JA
* MED 17.3.1
* MED 17.5.5
* EDC 15.x


## Android

The framework was verified on the following versions of Android

* 7
* 8
* 9
* 10
* 11


## Guides
* [Simple integration guide](doc/guides/integration/integration_guide.md)
* [Examples](doc/guides/examples/examples.md)
