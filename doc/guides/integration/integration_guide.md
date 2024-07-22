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

    implementation 'io.github.tzebrowski:obd-metrics:9.30.0'


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



#### Implementing the Bluetooth connector

Framework communicates with the OBD adapter using `StreamConnection` interface that mainly exposes `OutputStream` and `InputStream` streams.
`StreamConnection` object is mandatory when creating the `Workflow` so that concrete implementation must be provided, typical Bluetooth Android implementation can look like bellow.

<details>
<summary>Code example</summary>
<p>


```kotlin

private const val LOGGER_TAG = "BluetoothConnection"
private val RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class BluetoothConnection(private val deviceName: String) : AdapterConnection {

    private var input: InputStream? = null
    private var output: OutputStream? = null
    private lateinit var socket: BluetoothSocket

    init {
        Log.i(LOGGER_TAG, "Created instance of BluetoothConnection with devices: $deviceName")
    }

    override fun reconnect() {
        Log.i(LOGGER_TAG, "Reconnecting to the device: $deviceName")
        close()
        TimeUnit.MILLISECONDS.sleep(1000)
        connectToDevice()
        Log.i(LOGGER_TAG, "Successfully reconnect to the device: $deviceName")
    }

    override fun connect() {
        connectToDevice()
    }

    override fun close() {

        try {
            input?.close()
        } catch (_: Throwable){}

        try {
            output?.close()
        } catch (_: Throwable){}

        try {
            if (::socket.isInitialized)
                socket.close()
        } catch (_: Throwable){}

        Log.i(LOGGER_TAG, "Socket for the device: $deviceName is closed.")
    }

    override fun openOutputStream(): OutputStream? {
        return output
    }

    override fun openInputStream(): InputStream? {
        return input
    }

    private fun connectToDevice() {
        try {
            Log.i(
                LOGGER_TAG,
                "Found bounded connections, size: ${network.bluetoothAdapter()?.bondedDevices?.size}"
            )

            network.findBluetoothAdapterByName(deviceName)?.let { adapter ->
                Log.i(
                    LOGGER_TAG,
                    "Opening connection to bounded device: ${adapter.name}"
                )
                socket =
                    adapter.createRfcommSocketToServiceRecord(RFCOMM_UUID)
                socket.connect()
                Log.i(LOGGER_TAG, "Doing socket connect for: ${adapter.name}")

                if (socket.isConnected) {
                    Log.i(
                        LOGGER_TAG,
                        "Successfully established connection for: ${adapter.name}"
                    )
                    input = socket.inputStream
                    output = socket.outputStream
                    Log.i(
                        LOGGER_TAG,
                        "Successfully opened  the sockets to device: ${adapter.name}"
                    )
                }

            }
        }catch (e: SecurityException){
            network.requestBluetoothPermissions()
        }
    }
}

```
</p>
</details>


#### Implementing the OBD Metrics collector 

Framework implements Pub-Sub model to achieve low coupling between metric collection and metrics processing and in order to receive  the metrics it is required to register subscriber that gets notifications when metrics got read from the adapter.
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


#### Implementation of the Life-cycle observer

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
