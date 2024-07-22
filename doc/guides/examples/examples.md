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
var connection = MockAdapterConnection.builder()
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
var connection = MockAdapterConnection.builder()
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
var connection = MockAdapterConnection.builder()
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
var connection = MockAdapterConnection.builder()
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
var connection = MockAdapterConnection.builder()
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