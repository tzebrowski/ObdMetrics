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
import org.obd.metrics.api.Init;
import org.obd.metrics.api.Init.Header;
import org.obd.metrics.api.Init.Protocol;
import org.obd.metrics.api.Pids;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * OBD-request ID 11 bit functional: 0x7DF, psysical: 0x7E0 29 bit functional:
 * 0x18DB33F1, psysical: 0x18DA10F1 OBD-response 11 bit ECU1: 0x7E8, ECU2:
 * 0x7E9, ECU3: 0x7EA 29 bit ECU1: 0x18DAF110, ECU2: 0x18DAF118, ECU3:
 * 0x18DAF128
 */
@Slf4j
public class MultiModeIntegrationTest {

	@Test
	public void mode22() throws IOException, InterruptedException, ExecutionException {

		try (final InputStream mode22 = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("alfa.json")) {

			// Create an instance of PidRegistry that hold PID's configuration
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder()
			        .source(mode22)
			        .build();

			// Create an instance of CommandBuffer that holds the commands executed against
			// OBD Adapter
			CommandsBuffer buffer = CommandsBuffer.instance();

			// Query for specified PID's like: Estimated oil temperature
			buffer.add(AlfaMed17CommandGroup.INIT)
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
					        log.info("{}", t);
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
		//
		try (final InputStream mode01 = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			// Create an instance of PidRegistry that hold PID's configuration
			PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder()
			        .source(mode01).build();

			// Create an instance of CommandBuffer that holds the commands executed against
			// OBD Adapter
			CommandsBuffer buffer = CommandsBuffer.instance();
			// Query for specified PID's like: Estimated oil temperature
			buffer.add(DefaultCommandGroup.INIT)
			        .addLast(new ATCommand("SP7"))
			        
			        .addLast(new ATCommand("SH DB33F1"))
			        .addLast(new ObdCommand("01 05 0B"))

			        .addLast(new ATCommand("SH DA10F1"))
			        .addLast(new ObdCommand("22 1003 194F 1827"))

			        .addLast(new ATCommand("SH DB33F1"))
			        .addLast(new ObdCommand("01 05"))
			        
			        .addLast(new ATCommand("SH DA10F1"))
			        .addLast(new ObdCommand("22 1003 194F"))
			        
			        .addLast(new ATCommand("SH DB33F1"))
			        .addLast(new ObdCommand("01 05"))
			        .addLast(new QuitCommand());

			// quit the CommandExecutor

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
					        log.info("{}", t);
				        }
			        })
			        .codecs(codecRegistry)
			        .build();

			ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(commandLoop));

			Thread.sleep(5000);
			executorService.shutdown();
		}
	}

	@Test
	public void multiModeTest() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.openConnection();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .observer(new ReplyObserver<Reply<?>>() {
			        @Override
			        public void onNext(Reply<?> t) {
				        log.info("{}", t);
			        }
		        })
		        .pids(Pids.DEFAULT)
		        .initialize();

		final Query query = Query.builder()
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position

		        .pid(6014l) // mass air flow target
		        .pid(6013l) // mass air flow
		        .pid(6007l) // IAT
		        .pid(6012l) // target manifold pressure
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .cacheConfig(
		                CacheConfig.builder()
		                        .resultCacheEnabled(Boolean.FALSE).build())
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

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
		        .header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(AlfaMed17CommandGroup.INIT).build();

		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.SECONDS.toMillis(20), () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
