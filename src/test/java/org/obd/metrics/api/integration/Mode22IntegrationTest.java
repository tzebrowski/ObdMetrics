package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustments;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.api.PidSpec;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mode22IntegrationTest {

	@Test
	public void mode22() throws IOException, InterruptedException, ExecutionException {

		try (final InputStream mode022 = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")) {

			// Create an instance of PidRegistry that hold PID's configuration
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder()
					.source(mode022)
					.build();

			// Create an instance of CommandBuffer that holds the commands executed against
			// OBD Adapter
			CommandsBuffer buffer = CommandsBuffer.instance();

			// Query for specified PID's like: Estimated oil temperature
			buffer.add(AlfaMed17CommandGroup.CAN_INIT)
			        .addLast(new ObdCommand(pidRegistry.findBy(15l))) // Estimated oil temp
			        .addLast(new ObdCommand(pidRegistry.findBy(8l))) // Coolant temp
			        .addLast(new ObdCommand(pidRegistry.findBy(7l))) // IAT
			        .addLast(new QuitCommand());// quit the CommandExecutor

			// Create an instance of CodecRegistry that will handle decoding incoming raw
			// OBD frames
			CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();

			// Connection for an OBD adapter
			AdapterConnection connection = BluetoothConnection.openConnection();

			// commandLoop that glue all the ingredients
			CommandLoop commandLoop = CommandLoop
			        .builder()
			        .connection(connection)
			        .buffer(buffer)
			        .pids(pidRegistry)
			        .observer(new ReplyObserver<Reply<?>>() {
						
						@Override
						public void onNext(Reply<?> t) {
							log.info("{}",t);
						}
					})
			        .codecs(codecRegistry)
			        .build();

			ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(commandLoop));

			Thread.sleep(2000);
			executorService.shutdown();
		}
	}
	
	
	@Test
	public void mode01() throws IOException, InterruptedException, ExecutionException {

		try (final InputStream mode01 = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json")) {

			// Create an instance of PidRegistry that hold PID's configuration
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder()
					.source(mode01).build();

			// Create an instance of CommandBuffer that holds the commands executed against
			// OBD Adapter
			CommandsBuffer buffer = CommandsBuffer.instance();

			// Query for specified PID's like: Estimated oil temperature
			buffer.add(Mode1CommandGroup.INIT)
			        .addLast(new ObdCommand(pidRegistry.findBy(6l))) // IAT
			        .addLast(new ObdCommand(pidRegistry.findBy(16l))) // Coolant temp
			        .addLast(new QuitCommand());// quit the CommandExecutor

			
			// Create an instance of CodecRegistry that will handle decoding incoming raw
			// OBD frames
			CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();

			// Connection for an OBD adapter
			AdapterConnection connection = BluetoothConnection.openConnection();

			// commandLoop that glue all the ingredients
			CommandLoop commandLoop = CommandLoop
			        .builder()
			        .connection(connection)
			        .buffer(buffer)
			        .pids(pidRegistry)
			        .observer(new ReplyObserver<Reply<?>>() {
						
						@Override
						public void onNext(Reply<?> t) {
							log.info("{}",t);
						}
					})
			        .codecs(codecRegistry)
			        .build();

			ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(commandLoop));

			Thread.sleep(2000);
			executorService.shutdown();
		}
	}
	
	
	@Test
	public void mode22WorkflowTest() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.openConnection();
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .observer(new ReplyObserver<Reply<?>>() {
					
					@Override
					public void onNext(Reply<?> t) {
						log.info("{}",t);
					}
				})
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT)
		                .pidFile(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build())
		        .initialize();

		final Query query = Query.builder()
				.pid(15l) // Oil temp
		        .pid(8l) // Coolant
		        .pid(7l) // IAT
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .initDelay(1000)
		        .cacheConfig(
		        		CacheConfig.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheFilePath("./result_cache.json")
		        		.resultCacheEnabled(Boolean.TRUE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.SECONDS.toMillis(20), () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
