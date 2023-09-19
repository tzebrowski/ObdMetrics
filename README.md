# OBD Metrics

![CI](https://github.com/tzebrowski/ObdMetrics/workflows/Deploy/badge.svg?branch=main) 
![codecov](https://codecov.io/gh/tzebrowski/ObdMetrics/branch/main/graph/badge.svg)
![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.tzebrowski/obd-metrics/badge.svg)


## About

`OBD Metrics` is a Java OBD2 framework that is intended to simplify communication with OBD2 adapters like ELM327/STNxx clones.
The goal behind the implementation is to provide the extensionable framework which covers selected aspects of communication with the OBD2 adapters like reading OBD telemetry data and can be a foundation for the future OBD2 oriented applications. 


![Alt text](./src/main/resources/highlevel.jpg?raw=true "Big Picture")

#### Supported use-cases:
* Collecting vehicle telemetry data (Metrics)
* Reading Vehicle Metadata, e.g: VIN
* Reading Diagnostic Trouble Code (DTC)

#### Supported Adapters:
* Elm327 adapters 
* STNxx adapters

#### Example usage of the framework:

* [ObdGraphs](https://github.com/tzebrowski/ObdGraphs "ObdGraphs")   
* [OBD Metrics Demo](https://github.com/tzebrowski/ObdMetricsDemo "ObdMetricsDemo") 


## What makes this framework unique ?

#### Multiple sources of OBD2 PIDs/Sensors definitions

The framework uses external JSON resource files which defines series of supported PIDs. OBD2 PIDs/Sensors are divided into distinct groups. 

There are specified following OBD2 PIDs/Sensors categories:
- `capabilities` - Supported PIDs category  
- `dtc` - Diagnostic trouble code category
- `metadata` - Metadata PIDs category. PIDs which are read just once during session with the Adapter
- `livedata` - Livedata PIDs category. PIDs which are read frequently during session with the Adapter


Configuration might looks like the below example.

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
	]
}
```

Framework is able to work with multiple sources of PIDs which might be specific for different automotive manufacturers.
Generic list of PIDs can be found [here](./src/main/resources/mode01.json "mode01.json")

#### Communication with different ECU's within the same session

* The framework is able to query multiple ECU's within the same session based on different source of PID's, mode's and CAN filters.
It's able to work either with CAN 11 bit or CAN 29 bit headers.

<details>
<summary>Example</summary>
<p>

```java

final AdapterConnection connection = BluetoothConnection.openConnection();
final Pids pids = Pids
        .builder()
        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();

final Query query = Query.builder()
        .pid(6013l)  //Fiat specific
        .pid(6014l) // Fiat specific
        .pid(6005l) // Fiat specific
        
        .pid(13l) // Engine RPM
        .pid(12l) // Boost
        .pid(18l) // Throttle position
        .pid(14l) // Vehicle speed
        .pid(5l)  // Engine load
        .pid(7l)  // Short fuel trim
        .build();

final Init init = Init.builder()
        .delay(1000)
        .header(Header.builder().mode("22").header("DA10F1").build())
        .header(Header.builder().mode("01").header("DB33F1").build())
        .protocol(Protocol.CAN_29)
        .sequence(DefaultCommandGroup.INIT).build();

final Workflow workflow = Workflow
        .instance()
        .pids(pids)
        .initialize();

workflow.start(connection, query, init, optional);
```

</p>
</details> 


#### CAN header overrides 

* FW allows to override CAN headers just for specific PID's, and adjust it at runtime.

<details>
<summary>Configuration</summary>
<p>

```json
{
	"priority": 5,
	"id": "7033",
	"mode": "22",
	"pid": "0101",
	"length": 3,
	"description": "Distance till\n next service",
	"min": "0",
	"max": "30000",
	"units": "km",
	"type": "INT",
	"formula": "x=A.toString(16) + B.toString(16) + C.toString(16); parseInt(x,16)/10",
	"overrides" : {
		"canMode": "444",
		"batchEnabled": false
	}
},
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


final Init init = Init.builder()
       .delayAfterInit(0)
       .header(Header.builder().mode("22").header("DA10F1").build())
		.header(Header.builder().mode("01").header("DB33F1").build())
		//overrides CAN mode
		.header(Header.builder().mode("555").header("DA18F1").build()) 
       .protocol(Protocol.CAN_29)
       .sequence(DefaultCommandGroup.INIT).build();


```


#### Diagnostics interface

The frameworks collects metadata about commands processing, you can easily get information about *max*, *min*, *mean*, value for the current session with ECU.
 

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



#### Dynamic formula calculation

The framework is able to calculate PID's value from the RAW data using dynamic formulas written in JavaScipt.  
The formula can include additional JavaScript functions like *Math.floor* .
This makes, that there is no need to add an additional java class to support the new PID, it is just enough to update the JSON PID file with a new formula.


*Target overbost*
 
```  
(0.079 * (256*A + B))|0
```



#### Custom decoders

Framework has following custom decoders 

* VIN decoder `0902`, details;  [VinCommand](./src/main/java/org/obd/metrics/command/VinCommand.java "VinCommand.java") 
* Supported PIDS decoder `01 00, 01 20, ...`, details: [SupportedPidsCommand](./src/main/java/org/obd/metrics/command/obd/SupportedPidsCommand.java "SupportedPidsCommand.java") 



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

The framework supports `batch commands` and allows to ask for up to 6 PID's in a single request. 

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
In the pre-integration tests where the FW API is verified its possible to use `SimpleMockConnection` that simulates behavior of the real OBD adapter.



##  Framework 

The framework consists of multiple components that are intended to exchange the messages with the Adapter (Request-Response model) and propagate decoded metrics to the target application using a non-blocking manner (Pub-Sub model). All the internal details like managing multiple threads are hidden and the target application that includes FW must provide just a few interfaces that are required for establishing the connection with the Adapter and receiving the OBD metrics.

 
API of FW is exposed through `Workflow` interface which centralize all the features in the single place, see: [Workflow](./src/main/java/org/obd/metrics/api/Workflow.java "Workflow.java").
Particular workflow implementations can be instantiated by [WorkflowFactory](./src/main/java/org/obd/metrics/api/WorkflowFactory.java "WorkflowFactory.java")




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
 * <li>Collecting the OBD metrics</li>
 * <li>Obtain statistics registry</li>
 * <li>Obtain pid's registry</li>
 * <li>Gets notifications about errors that appears during interaction with the
 * device.</li>
 * 
 * </ul>
 * 
 * @version 4.0.0
 * @see Adjustments
 * @see AdapterConnection
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

    /**
     * It starts the process of collecting the OBD metrics
     * 
     * @param connection the connection to the Adapter (parameter is mandatory)
     * @param query      queried PID's (parameter is mandatory)
     */
    default void start(@NonNull AdapterConnection connection, @NonNull Query query) {
        start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);
    }

    /**
     * It starts the process of collecting the OBD metrics
     * 
     * @param connection  the connection to the Adapter (parameter is mandatory)
     * @param query       queried PID's (parameter is mandatory)
     * @param adjustments additional settings for process of collection the data
     */
    default void start(@NonNull AdapterConnection connection, @NonNull Query query, Adjustments adjustments) {
        start(connection, query, Init.DEFAULT, adjustments);
    }

    /**
     * It starts the process of collecting the OBD metrics
     * 
     * @param adjustements additional settings for process of collection the data
     * @param connection   the connection to the Adapter (parameter is mandatory)
     * @param query        queried PID's (parameter is mandatory)
     * @param init         init settings of the Adapter
     */
    void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
            Adjustments adjustements);

    /**
     * Stops the current workflow.
     */
    void stop();

    /**
     * Gets the current pid registry for the workflow.
     * 
     * @return instance of {@link PidDefinitionRegistry}
     */
    PidDefinitionRegistry getPidRegistry();

    /**
     * Gets diagnostics collected during the work.
     * 
     * @return instance of {@link Diagnostics}
     */
    Diagnostics getDiagnostics();
}

```
</p>
</details> 





## Working code examples

More working examples can be found within the API tests directory.

### Examples 

<details>
<summary>Enabling batch commands</summary>
<p>

```java

//Create an instance of DataCollector that receives the OBD Metrics
var collector = new DataCollector();

//Getting the Workflow instance for mode 01
var workflow = SimpleWorkflowFactory.getMode01Workflow(collector);

//Query for specified PID's like: Engine coolant temperature
var query = Query.builder()
        .pid(6l) // Engine coolant temperature
        .pid(12l) // Intake manifold absolute pressure
        .pid(13l) // Engine RPM
        .pid(16l) // Intake air temperature
        .pid(18l) // Throttle position
        .pid(14l) // Vehicle speed
        .build();

//Create an instance of mock connection with additional commands and replies 
var connection = SimpleMockConnection.builder()
        .commandReply("0100", "4100be3ea813")
        .commandReply("0200", "4140fed00400")
        .commandReply("01 0B 0C 11 0D 0F 05 3", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa").build();

//Enabling batch commands
var optional = Adjustments
        .builder()
        .batchEnabled(true)
        .build();

//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
workflow.start(connection, query, optional);

// Starting the workflow completion job, it will end workflow after some period of time (helper method)
WorkflowFinalizer.finalizeAfter500ms(workflow);

// Ensure we receive AT commands
Assertions.assertThat(collector.findATResetCommand()).isNotNull();

var coolant = workflow.getPidRegistry().findBy(6l);

// Ensure we receive Coolant temperatur metric
var metrics = collector.findMetricsBy(coolant);
Assertions.assertThat(metrics.isEmpty()).isFalse();
var metric = metrics.iterator().next();

Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
Assertions.assertThat(metric.getValue()).isEqualTo(-40);

```

</p>
</details> 



<details>
<summary>Getting the VIN</summary>
<p>

```java

//Specify lifecycle observer
var lifecycle = new SimpleLifecycle();

//Specify the metrics collector
var collector = new DataCollector();

//Obtain the Workflow instance for mode 01
var workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle, collector);

//Define PID's we want to query
var query = Query.builder()
        .pid(6l) // Engine coolant temperature
        .pid(12l) // Intake manifold absolute pressure
        .pid(13l) // Engine RPM
        .pid(16l) // Intake air temperature
        .pid(18l) // Throttle position
        .pid(14l) // Vehicle speed
        .build();

//Define mock connection  with VIN data "09 02" command
var connection = SimpleMockConnection.builder()
        .commandReply("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
        .commandReply("0100", "4100be3ea813")
        .commandReply("0200", "4140fed00400")
        .commandReply("0105", "410522")
        .commandReply("010C", "410c541B")
        .commandReply("010B", "410b35")
        .build();

//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
workflow.start(connection, query);

// Starting the workflow completion job, it will end workflow after some period of time (helper method)
WorkflowFinalizer.finalizeAfter500ms(workflow);

// Ensure we receive AT command
Assertions.assertThat(collector.findATResetCommand()).isNotNull();


//Ensure Device Properties Holder contains VIN 0140:4902015756571:5A5A5A314B5A412:4D363930333932 -> WVWZZZ1KZAM690392
Assertions.assertThat(lifecycle.getProperties()).containsEntry("VIN", "WVWZZZ1KZAM690392");

```

</p>
</details> 


<details>
<summary>Priority commands</summary>
<p>

```java

// Getting the workflow - mode01
var workflow = SimpleWorkflowFactory.getMode01Workflow();

// Specify more than 6 commands, so that we have 2 groups
var query = Query.builder()
        .pid(6l)  // Engine coolant temperature
        .pid(12l) // Intake manifold absolute pressure
        .pid(13l) // Engine RPM
        .pid(16l) // Intake air temperature
        .pid(18l) // Throttle position
        .pid(14l) // Vehicle speed
        .pid(15l) // Timing advance
        .build();


//Define PID's we want to query, 2 groups, RPM should be queried separately 
var connection = SimpleMockConnection.builder()
        .commandReply("0100", "4100be3ea813")
        .commandReply("0200", "4140fed00400")
        .commandReply("01 05", "410500") // group 1, slower one
        .commandReply("01 0B 0C 11 0D 0E 0F", "00e0:410bff0c00001:11000d000e800f2:00aaaaaaaaaaaa") // group 2, fast group
        .build();

//Enable priority commands
var optional = Adjustments.builder()
        .batchEnabled(true)
        .producerPolicy(
                ProducerPolicy.builder()
                        .priorityQueueEnabled(Boolean.TRUE)
                        .lowPriorityCommandFrequencyDelay(100)
                        .build())
        .build();

//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
workflow.start(connection, query, optional);

var p1 = workflow.getPidRegistry().findBy(6l);// Engine coolant temperature
var p2 = workflow.getPidRegistry().findBy(13l);// Engine RPM
var statisticsRegistry = workflow.getStatisticsRegistry();

runCompletionThread(workflow, p1, p2);

var rate1 = statisticsRegistry.getRatePerSec(p1);
var rate2 = statisticsRegistry.getRatePerSec(p2);

log.info("Pid: {}, rate: {}", p1.getDescription(), rate1);
log.info("Pid: {}, rate: {}", p2.getDescription(), rate2);

Assertions.assertThat(rate1).isGreaterThan(0);
Assertions.assertThat(rate2).isGreaterThan(0);

// Engine coolant temperatur should have less RPS than RPM
Assertions.assertThat(rate1).isLessThanOrEqualTo(rate2);

```

</p>
</details>



<details>
<summary>Adaptive timing</summary>
<p>

```java

//Create an instance of DataCollector that receives the OBD Metrics
var collector = new DataCollector();

//Getting the Workflow instance for mode 22
var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

//Query for specified PID's like: Engine coolant temperature
var query = Query.builder()
        .pid(8l) // Coolant
        .pid(4l) // RPM
        .pid(7l) // Intake temp
        .pid(15l)// Oil temp
        .pid(3l) // Spark Advance
        .build();

//Create an instance of mock connection with additional commands and replies 
var connection = SimpleMockConnection.builder()
        .commandReply("221003", "62100340")
        .commandReply("221000", "6210000BEA")
        .commandReply("221935", "62193540")
        .commandReply("22194f", "62194f2d85")
// Set read timeout for every character,e.g: inputStream.read(), we want to ensure that initial timeout will be decrease during the tests                   
        .readTimeout(1) //
        .build();

// Set target frequency
var targetCommandFrequency = 4;

// Enable adaptive timing
var optional = Adjustments
        .builder()
        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
                .builder()
                .enabled(Boolean.TRUE)
                .checkInterval(20)// 20ms
                .commandFrequency(targetCommandFrequency + 2)
                .build())
        .build();

//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
workflow.start(connection, query, optional);

var rpm = workflow.getPidRegistry().findBy(4l);

// Starting the workflow completion job, it will end workflow after some period of time (helper method)
setupFinalizer(targetCommandFrequency, workflow, rpm);

// Ensure we receive AT command
Assertions.assertThat(collector.findATResetCommand()).isNotNull();

// Ensure target command frequency is on the expected level
var ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(rpm);
Assertions.assertThat(ratePerSec)
        .isGreaterThanOrEqualTo(targetCommandFrequency);

```

</p>
</details>

<details>
<summary>Collecting metrics for mode 22</summary>
<p>

```java

//Create an instance of DataCollector that receives the OBD Metrics
var collector = new DataCollector();

//Create an instance of the Mode 22 Workflow
var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

//Query for specified PID's like RPM
var query = Query.builder()
        .pid(8l) // Coolant
        .pid(4l) // RPM
        .pid(7l) // Intake temp
        .pid(15l)// Oil temp
        .pid(3l) // Spark Advance
        .build();

//Create an instance of mocked connection with additional commands and replies
var connection = SimpleMockConnection.builder()
        .commandReply("221003", "62100340")
        .commandReply("221000", "6210000BEA")
        .commandReply("221935", "62193540")
        .commandReply("22194f", "62194f2d85")
        .build();

//Extra settings for collecting process like command frequency 14/sec
var optional = Adjustments.builder()
        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
                .builder()
                .enabled(Boolean.TRUE)
                .checkInterval(20)// 20ms
                .commandFrequency(14).build())
        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
        .build();

//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
workflow.start(connection, query, optional);

var rpm = workflow.getPidRegistry().findBy(4l);

// Workflow completion thread, it will end workflow after some period of time (helper method)
setupFinalizer(workflow, rpm);

// Ensure we receive AT command as well
Assertions.assertThat(collector.findATResetCommand()).isNotNull();

var metrics = collector.findMetricsBy(rpm);
Assertions.assertThat(metrics.isEmpty()).isFalse();

// Ensure we receive  RPM metric
Assertions.assertThat(metrics.iterator().next().valueToDouble()).isEqualTo(762.5);
```

</p>
</details> 




## Integration guide


#### Adding the dependency 


`Obd Metrics` is released to the Maven Central and can be added as dependency without specifying additional repository, see: [search.maven.org](https://search.maven.org/artifact/io.github.tzebrowski/obd-metrics/1.0.0/jar "WorkflowFactory.java")

In order to add `obd-metrics` dependency to the Android project, `build.gradle` descriptor must be altered as listed bellow. 
Except `obd-metrics` there is a need to specify additional dependencies required by the library, like: `jackson`, `rxjava`, `rhino-android`.

<details>
<summary>build.gradle</summary>
<p>

```groovy
dependencies {

    implementation 'io.github.tzebrowski:obd-metrics:4.4.0'


    implementation 'io.dropwizard.metrics:metrics-core:4.1.17'
    implementation 'io.reactivex:rxjava:1.3.8'
    implementation 'io.apisense:rhino-android:1.1.1'
    implementation 'org.slf4j:slf4j-simple:1.7.5'
    implementation 'org.apache.commons:commons-collections4:4.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.11.0'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0'
}
```
</p>
</details>



#### Definition of the Bluetooth connection 

Framework communicates with the OBD adapter using `StreamConnection` interface that mainly exposes `OutputStream` and `InputStream` streams.
`StreamConnection` object is mandatory when creating the `Workflow` so that concrete implementation must be provided, typical Bluetooth Android implementation can look like bellow.

<details>
<summary>Code example</summary>
<p>


```kotlin

internal class BluetoothConnection : AdapterConnection {

    private val RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private lateinit var socket: BluetoothSocket
    private var device: String? = null
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    constructor(btDeviceName: String) {
        this.device = btDeviceName
    }

    override fun reconnect() {
        Log.i(LOG_KEY, "Reconnecting to the device: $device")
        input?.close()
        output?.close()
        socket.close()
        TimeUnit.MILLISECONDS.sleep(1000)
        connectToDevice(device)
        Log.i(LOG_KEY, "Successfully reconnect to the device: $device")
    }

    override fun connect() {
        connectToDevice(device)
    }

    override fun close() {
        if (::socket.isInitialized)
            socket.close()
        Log.i(LOG_KEY, "Socket for device: $device has been closed.")
    }

    override fun openOutputStream(): OutputStream? {
        return output
    }

    override fun openInputStream(): InputStream? {
        return input
    }

    override fun isClosed(): Boolean {
        return !socket.isConnected
    }

    private fun connectToDevice(btDeviceName: String?) {
        for (currentDevice in mBluetoothAdapter.bondedDevices) {
            if (currentDevice.name == btDeviceName) {
                Log.i(LOG_KEY, "Opening connection to device: $btDeviceName")
                socket =
                    currentDevice.createRfcommSocketToServiceRecord(RFCOMM_UUID)
                socket.connect()
                if (socket.isConnected) {
                    input = socket.inputStream
                    output = socket.outputStream
                    Log.i(
                        LOG_KEY,
                        "Successfully opened  the connection to device: $btDeviceName"
                    )
                    break
                }
            }
        }

    }
}
```
</p>
</details>


#### Definition of the OBD Metrics collector 

Framework implements Pub-Sub model to achieve low coupling between metric collection and metrics processing that happens normally in the target application. 
In order to receives  the OBD Metrics it is required to register subscriber that gets notifications when metrics got read from the adapter.
To do that, you must define a class that inherits from `ReplyObserver`, bellow you can find typical implementation.

<details>
<summary>Code example</summary>
<p>


```kotlin

internal class MetricsAggregator : ReplyObserver<Reply<*>>() {

    var data: MutableMap<Command, ObdMetric> = hashMapOf()

    override fun onNext(reply: Reply<*>) {
        debugData.postValue(reply)
        
        if (reply.command is ObdCommand && reply.command !is SupportedPidsCommand) {
            data[reply.command] = reply as ObdMetric
            (reply.command as ObdCommand).pid?.let {
                metrics.postValue(reply)
            }
        }
    }

    companion object {
        @JvmStatic
        val debugData: MutableLiveData<Reply<*>> = MutableLiveData<Reply<*>>().apply {
        }

        @JvmStatic
        val metrics: MutableLiveData<ObdMetric> = MutableLiveData<ObdMetric>().apply {
        }
    }
}
```
</p>
</details>


#### Life-cycle observer

Framework implements Pub-Sub model to notify about it life-cycle.
In order to gets notification about errors that occurs during processing, or status of connection to the device `Lifecycle` interface must be specified.
Bellow you can find example implementation.

<details>
<summary>Code example</summary>
<p>


```kotlin

 private var lifecycle = object : Lifecycle {
        override fun onConnecting() {
            Log.i(LOG_KEY, "Start collecting process for the Device: $device")
            modelUpdate.data.clear()
            context.sendBroadcast(Intent().apply {
                action = NOTIFICATION_CONNECTING
            })
        }

        override fun onConnected(deviceProperties: DeviceProperties) {
            Log.i(LOG_KEY, "We are connected to the device: $deviceProperties")
            context.sendBroadcast(Intent().apply {
                action = NOTIFICATION_CONNECTED
            })
        }

        override fun onError(msg: String, tr: Throwable?) {
            Log.e(
                LOG_KEY,
                "An error occurred during interaction with the device. Msg: $msg"
            )
            workflow().stop()
            context.sendBroadcast(Intent().apply {
                action = NOTIFICATION_ERROR
            })
        }

        override fun onStopped() {
            Log.i(
                LOG_KEY,
                "Collecting process completed for the Device: $device"
            )

            context.sendBroadcast(Intent().apply {
                action = NOTIFICATION_STOPPED
            })
        }

        override fun onStopping() {
            Log.i(LOG_KEY, "Stop collecting process for the Device: $device")

            context.sendBroadcast(Intent().apply {
                action = NOTIFICATION_STOPPING
            })
        }
    }
```
</p>
</details>


#### Declaration of the `Workflow` instance 

The `Workflow` implementation is a main part that controls the process of connecting to the OBD adapter and collecting OBD metrics. 
Normally should be specified within Android Service and you should always keep single instance of it.
 
<details>
<summary>Code example</summary>
<p>


```kotlin

var metricsAggregator = MetricsAggregator()
var mode1: Workflow = WorkflowFactory
    .mode1()
    .equationEngine("rhino")
    .pidSpec(
        PidSpec
            .builder()
            .initSequence(Mode1CommandGroup.INIT)
            .pidFile(Urls.resourceToUrl("mode01.json")).build()
    ).observer(metricsAggregator)
    .lifecycle(lifecycle)
    .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
        .builder()
        .enabled(true)
        .checkInterval(10000) // 10s
        .commandFrequency(7) // 7req/sec
        .build())
    .initialize()

```
</p>
</details>



#### Starting the process

In order to start the workflow, `start` operation must be called.
Calling that method launches multiply internal threads that sends commands to the adapter, decodes raw data, and populates `OBDMetrics` objects to all registered observers.  

<details>
<summary>Code example</summary>
<p>

```kotlin
fun start() {

val adapterName = "OBDII"
val query = Query.builder().pids(pref.getStringSet("pref.pids.generic", emptySet())!!).build()
val batchEnabled: Boolean = PreferencesHelper.isBatchEnabled(context)
val adjustments = Adjustments.builder()
        .batchEnabled(Preferences.isBatchEnabled(context))
        .generator(
            GeneratorSpec
                .builder()
                .smart(true)
                .enabled(Preferences.isEnabled(context, "pref.debug.generator.enabled"))
                .increment(0.5).build()
        )
        .adaptiveTimeoutPolicy(
            AdaptiveTimeoutPolicy
                .builder()
                .enabled(Preferences.isEnabled(context, "pref.adapter.adaptive.enabled"))
                .checkInterval(5000) //10s
                .commandFrequency(Preferences.getCommandFreq(context))
                .build()
        ).build()
        
mode1.start(BluetoothConnection(device.toString()),query,adjustments)        
   
}

```
</p>
</details

.

#### Stopping the process

In order to stop the workflow, `stop` operation must be called.
Calling that methods cause that all the working threads are blocked, and no communication with OBD adapter happens.

<details>
<summary>Code example</summary>
<p>


```kotlin
fun stop() {
  mode1.stop()
}   
```

</p>
</details

.

## Quality

Quality of the project is ensured by junit and integration tests. 
In order to ensure that coverage is on the right level since 0.0.3-SNAPTHOST jacoco check plugin is part of the build.
Minimum coverage ratio is set to 80%, build fails if not meet.
 



## Verified against 

Framework has been verified against following ECU.

* MED 17.3.1
* MED 17.5.5
* EDC 15.x




## Android

The framework was verified on the following versions of Android

* 7
* 8
* 9




