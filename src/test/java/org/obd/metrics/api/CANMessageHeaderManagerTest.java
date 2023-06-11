package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;

public class CANMessageHeaderManagerTest {
	
	@Test
	public void metadataTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
				.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
				.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
				.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
				.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchEnabled(Boolean.TRUE)
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,800);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();

		// initialization
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2CSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2C ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2DSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2D ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
		
		// getting vehicle properties
		// switching CAN header to mode22 
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F18C");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F194");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F191");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F192");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F187");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F196");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F195");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F193");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F1A5");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("222008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("221008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0902");
		
		// getting supported modes
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");
		
		// querying for pids
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
	
	@Test
	public void dtcReadTest() throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
				.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
				.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
				.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
				.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcReadingEnabled(Boolean.TRUE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchEnabled(Boolean.TRUE)
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,2000);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		System.out.println(recordedQueries);
		
		
		// initialization
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2CSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2C ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2DSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2D ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
	
		// getting vehicle properties
		// switching CAN header to mode22 
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F18C");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F194");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F191");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F192");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F187");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F196");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F195");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F193");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F1A5");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("222008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("221008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0902");
		// DTC reading
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
		// getting supported modes
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");
		
		// querying for pids
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
	
	
	@Test
	public void dtcClearTest() throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
				.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
				.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
				.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
				.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
		        .header(Header.builder().mode("14").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcCleaningEnabled(Boolean.TRUE)
		        .vehicleDtcReadingEnabled(Boolean.TRUE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchEnabled(Boolean.TRUE)
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 2000);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		
		// initialization
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2CSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2C ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2DSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2D ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
	
		// getting vehicle properties
		// switching CAN header to mode22 
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F18C");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F194");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F191");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F192");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F187");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F196");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F195");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F193");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F1A5");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("222008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("221008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0902");
		// DTC reading
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
		// getting supported modes
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");

		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("14FFFFFF");

		
		// querying for pids
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
}
