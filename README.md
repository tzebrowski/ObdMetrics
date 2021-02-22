# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the framework is to provide set of useful features that allows to collect and process vehicle metrics.
Example usage can be found under: [Android OBD2 data logger](https://github.com/tzebrowski/AlfaDataLogger "AlfaDataLogger") 


## What makes this framework unique ?

### Pid definitions

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


### Multiple decoders for the single PID

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

### Mockable device interfaces

There is not necessary to have physical ECU device to play with the framework. 
In the pre-integration tests where the API is verified its possible to use `MockConnection` that simulates behavior of the real device.


<details>
<summary>Usage in E2E tests</summary>
<p>

```java
DataCollector collector = new DataCollector();
final Workflow workflow = WorkflowFactory.generic()
        .ecuSpecific(EcuSpecific
            .builder()
            .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
            .pidFile("alfa.json").build())
        .observer(collector)
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

workflow.filter(ids).start(connection);
final Callable<String> end = () -> {
    Thread.sleep(1 * 1500);
    log.info("Ending the process of collecting the data");
    workflow.stop();
    return "end";
};

final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
newFixedThreadPool.invokeAll(Arrays.asList(end));
newFixedThreadPool.shutdown();

//Ensure we receive AT command as well
Reply<?> at = collector.getData().get(new ResetCommand()).iterator().next();
Assertions.assertThat(at).isNotNull();

ObdMetric metric = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(4l))).iterator().next();
Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
Assertions.assertThat(metric.getValue()).isEqualTo(762.5);

```

</p>
</details> 

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


### Custom decoders

Framework has following custom decoders 

* VIN decoder: 0902
* supported PIDS 01 00, 01 20, ...


##  API

API of the framework is exposed through the [Workflow](./src/main/java/org/obd/metrics/api/Workflow.java "Workflow.java") interface.
Particular workflow implementations can be instantiated by [WorkflowFactory](./src/main/java/org/obd/metrics/api/WorkflowFactory.java "WorkflowFactory.java")
`Workflow` from the one hand expose all the features outside, but from the other hide all the complexity that happens inside, like threads management.


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
    PidRegistry getPids();

    /**
     * Gets statistics collected during work.
     * 
     * @return statistics instance of {@link StatisticsAccumulator}
     */
    StatisticsAccumulator getStatistics();
}

```

</p>
</details> 



## Integration guide


### Adding the dependency 

In order to add `obd-metrics` dependency to the Android project, `build.gradle` descriptors (2) must be altered as specified bellow.

Main `build.gradle`

```
allprojects {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }
}


```

Module  `build.gradle`


```
dependencies {
    implementation 'io.dropwizard.metrics:metrics-core:4.1.17'
    implementation 'io.reactivex:rxjava:1.3.8'
    implementation 'io.apisense:rhino-android:1.1.1'
    implementation 'org.slf4j:slf4j-simple:1.7.5'
    implementation 'org.apache.commons:commons-collections4:4.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.11.0'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0'
   

    implementation ('io.github.tzebrowski:obd-metrics:0.0.2-SNAPSHOT'){ changing = true }
}

```


#### Defining the `Workflow` instance 


Declaration of the `Workflow` and `ReplyObserver` instances, this code normally should be part of the Android Service.
 
<details>
<summary>Code example</summary>
<p>


```kotlin

var modelUpdate = ModelChangePublisher()
var mode1: Workflow =
WorkflowFactory.mode1().equationEngine("rhino")
    .ecuSpecific(
        EcuSpecific
            .builder()
            .initSequence(Mode1CommandGroup.INIT)
            .pidFile("mode01.json").build()
    )
    .observer(modelUpdate)
    .statusObserver(statusObserver)
    .commandFrequency(80)
    .initialize()

```
</p>
</details>


### Definition of the OBD Metrics collector 


`ModelChangePublisher` class in the method `onNext` receives OBD metrics when collecting process starts.


<details>
<summary>Code example</summary>
<p>


```kotlin

internal class ModelChangePublisher : ReplyObserver() {

    override fun onNext(reply: Reply<*>) {
        data.postValue(reply)
    }

    companion object {
        @JvmStatic
        val data: MutableLiveData<Reply<*>> = MutableLiveData<Reply<*>>().apply {
        }
    }
}
```
</p>
</details>


### Starting the process

In order to start the workflow, `stop` operation must be called.

<details>
<summary>Code example</summary>
<p>

```kotlin
fun start() {

    var adapterName = "OBDII"
    var selectedPids = pref.getStringSet("pref.pids.generic", emptySet())!!
    var batchEnabled: Boolean = PreferencesHelper.isBatchEnabled(context)

    mode1.filter(selectedPids.map { s -> s.toLong() }.toSet())
        .batch(batchEnabled).start(BluetoothConnection(deviceadapterName))
}
```

</p>
</details

### Stopping the process

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


