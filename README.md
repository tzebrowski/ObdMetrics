# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the framework is to provide set of useful features that allows to collect and process vehicle metrics.
Example usage can be found under: [Android OBD2 data logger](https://github.com/tzebrowski/AlfaDataLogger "AlfaDataLogger") 


## What makes this framework unique ?

#### Pid definitions

* Framework uses external JSON files that defines series of supported PID's (SAE J1979) and evaluations formula. Default configuration has following structure 

```json
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



#### Batch commands

Framework allows to ask for up to 6 PID's in a single request.

*Request:*

``` 
01 01 03 04 05 06 07
```

*Response:*

``` 
0110:4101000771611:0300000400051c2:06800781000000
```


#### Dynamic formula calculation

* Framework is able to calculate equation defined within Pid's definition to get PID value. 
It may include additional JavaScript functions like *Math.floor* ..

``` 
Math.floor(((A*256)+B)/32768((C*256)+D)/8192)
```

#### Priority commands

It's possible to set priority for some of the PID's so they are pulled from the Adapter more frequently than others. 
A good example is an `RPM` or `Boost pressure` that should be queried more often because of its characteristics over time than `Engine Coolant Temperature`.


#### Support for 22 mode

* It has support for mode 22 PIDS
* Configuration: [alfa.json](./src/main/resources/alfa.json?raw=true "alfa.json")
* Integration test: [AlfaIntegrationTest](./src/test/java/org/obd/metrics/integration/AlfaIntegrationTest.java "AlfaIntegrationTest.java") 


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



#### Mocking OBD Adapter

There is not necessary to have physical ECU device to play with the framework. 
In the pre-integration tests where the FW API is verified its possible to use `MockConnection` that simulates behavior of the real OBD adapter.


<details>
<summary>Usage in E2E tests</summary>
<p>

```java
final DataCollector collector = new DataCollector();
final Workflow workflow = WorkflowFactory.generic()
        .pidSpec(PidSpec
                .builder()
                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
                .pidFile(Urls.resourceToUrl("alfa.json")).build())
        .observer(collector)
        .adaptiveTiming(AdaptiveTimeoutPolicy.builder().commandFrequency(14).build())
        .initialize();

final Set<Long> ids = new HashSet<>();
ids.add(8l); // Coolant
ids.add(4l); // RPM
ids.add(7l); // Intake temp
ids.add(15l);// Oil temp
ids.add(3l); // Spark Advance

final MockConnection connection = MockConnection.builder()
        .commandReply("221003", "62100340")
        .commandReply("221000", "6210000BEA")
        .commandReply("221935", "62193540")
        .commandReply("22194f", "62194f2d85")
        .build();

workflow.start(WorkflowContext
        .builder()
        .connection(connection)
        .filter(ids).build());
        
final Callable<String> end = () -> {
    Thread.sleep(1 * 5000);
    log.info("Ending the process of collecting the data");
    workflow.stop();
    return "end";
};

final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
newFixedThreadPool.invokeAll(Arrays.asList(end));
newFixedThreadPool.shutdown();

// Ensure we receive AT command as well
Reply<?> at = collector.getData().get(new CustomATCommand("Z")).iterator().next();
Assertions.assertThat(at).isNotNull();

final PidDefinition pid = workflow.getPidRegistry().findBy(4l);
Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid))
        .isGreaterThan(10);

ObdMetric metric = (ObdMetric) collector.getData().get(new ObdCommand(pid))
        .iterator()
        .next();
Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
Assertions.assertThat(metric.getValue()).isEqualTo(762.5);

```

</p>
</details> 





##  API

Framework implements Pub-Sub model for propagation of the internal state and OBD metrics.
API of FW is exposed through `Workflow` interface which centralize all the features in the single place, see: [Workflow](./src/main/java/org/obd/metrics/api/Workflow.java "Workflow.java").
Particular workflow implementations can be instantiated by [WorkflowFactory](./src/main/java/org/obd/metrics/api/WorkflowFactory.java "WorkflowFactory.java")

<details>
<summary>Workflow interface</summary>
<p>


```java

/**
 * Thats is the main interface that expose the API of the framework. It contains
 * typical operations that allows to play with the OBD adapters like:
 * <ul>
 * <li>connecting to the device</li>
 * <li>collecting the the OBD metrics</li>
 * <li>gets notifications about errors that appears during interaction with the
 * device.</li>
 * </ul>
 * 
 * Typically instance of the Workflow is create by {@link WorkflowFactory}, see
 * it for details.
 * 
 * @see WorkflowFactory
 * @see WorkflowContext
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

    /**
     * It starts the process of collecting the OBD metrics
     * 
     * @param context instance of the {@link WorkflowContext}
     */
    void start(@NonNull WorkflowContext context);

    /**
     * Stops the current workflow.
     */
    void stop();

    /**
     * Gets the current pid registry for the workflow.
     * 
     * @return instance of {@link PidRegistry}
     */
    PidRegistry getPidRegistry();

    /**
     * Gets statistics collected during the work.
     * 
     * @return statistics instance of {@link StatisticsRegistry}
     */
    StatisticsRegistry getStatisticsRegistry();

}

```
</p>
</details> 



## Integration guide


#### Adding the dependency 

In order to add `obd-metrics` dependency to the Android project, `build.gradle` descriptors (2) must be altered as specified bellow.
 
*Project file* details:

<details>
<summary>build.gradle</summary>
<p>

```groovy
allprojects {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }
}
```

</p>
</details>

*Module  file* details:

<details>
<summary>build.gradle</summary>
<p>

```groovy
dependencies {
    implementation 'io.dropwizard.metrics:metrics-core:4.1.17'
    implementation 'io.reactivex:rxjava:1.3.8'
    implementation 'io.apisense:rhino-android:1.1.1'
    implementation 'org.slf4j:slf4j-simple:1.7.5'
    implementation 'org.apache.commons:commons-collections4:4.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.11.0'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0'
   

    implementation ('io.github.tzebrowski:obd-metrics:0.3.0-SNAPSHOT'){ changing = true }
}
```
</p>
</details>



#### Definition of the Bluetooth connection 

Framework communicates with the OBD adapter using `Connection` interface that mainly exposes `OutputStream` and `InputStream` streams.
`Connection` object is mandatory when creating the `Workflow` so that concrete implementation must be provided, typical Bluetooth Android implementation can look like bellow.

<details>
<summary>Code example</summary>
<p>


```kotlin

internal class BluetoothConnection : Connection {

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
In order to receives  the OBD Metrics it is required to register subscriber that will get notifications when metrics got read from the adapter.
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
    .adaptiveTiming(AdaptiveTimeoutPolicy
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

<details>
<summary>Code example</summary>
<p>

```kotlin
fun start() {

    var adapterName = "OBDII"
    var selectedPids = pref.getStringSet("pref.pids.generic", emptySet())!!
    var batchEnabled: Boolean = PreferencesHelper.isBatchEnabled(context)
   
    var ctx = WorkflowContext.builder()
        .filter(selectedPids.map { s -> s.toLong() }.toSet())
        .batchEnabled(PreferencesHelper.isBatchEnabled(context))
        .connection(BluetoothConnection(device.toString())).build()
    mode1.start(ctx)
   
}
```

</p>
</details


#### Stopping the process

In order to stop the workflow, `stop` operation must be called.

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

....


#### Working examples

Working example can be found within API tests directory.



## Quality

Quality of the project is ensured by junit and integration tests. 
In order to ensure that coverage is on the right level since 0.0.3-SNAPTHOST jacoco check plugin is part of the build.
Minimum code ratio is set to 80% of coverage. 


## Supported devices

Framework has been verified against following OBD adapters.

* ELM 1.5
* ELM 2.2


## Verified against 

Framework has been verified against following ECU.

* MED 17.3.1
* MED 17.5.5
* EDC 15.x



## Design view


TBD

<details>
<summary>Component view</summary>
<p>


![Alt text](./src/main/resources/component.png?raw=true "Component view")

</p>
</details


TBD

<details>
<summary>Model view</summary>
<p>


![Alt text](./src/main/resources/model.png?raw=true "Model view")


</p>
</details


# Architecture drivers

1. Extensionality
2. Reliability




