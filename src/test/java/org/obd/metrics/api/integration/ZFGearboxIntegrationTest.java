package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.CommandLoop;
import org.obd.metrics.api.ConnectionManager;
import org.obd.metrics.api.Resources;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ZFGearboxIntegrationTest {

	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.transport.DefaultConnector");
		logger.setLevel(Level.TRACE);
				 
		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);

		
		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();
		
		final PidDefinitionRegistry pidRegistry = toPidRegistry(pids);

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SPB"));
		buffer.addLast(new ATCommand("S0"));
		buffer.addLast(new ATCommand("AL"));
		buffer.addLast(new ATCommand("CP18"));
//		buffer.addLast(new ATCommand("CRA18DAF118"));
//		buffer.addLast(new ATCommand("SHDA18F1"));
		buffer.addLast(new ATCommand("AT1"));
		buffer.addLast(new ATCommand("ST99"));

		buffer.addLast(new ObdCommand("222023"));
//		buffer.addLast(new ObdCommand("222024"));
//		buffer.addLast(new ObdCommand("22F1A5"));
//		buffer.addLast(new ObdCommand("22F190"));
//		buffer.addLast(new ObdCommand("22F18C"));
//		buffer.addLast(new ObdCommand("22F187"));
//		buffer.addLast(new ObdCommand("22F192"));
//		buffer.addLast(new ObdCommand("22F193"));
//		buffer.addLast(new ObdCommand("22F194"));
//		buffer.addLast(new ObdCommand("22F195"));
//		buffer.addLast(new ObdCommand("22F196"));
//		buffer.addLast(new ObdCommand("22F191"));
//
//		buffer.addLast(new ObdCommand("22 051A"));
//		buffer.addLast(new ObdCommand("22 1018"));
//		buffer.addLast(new ObdCommand("22 04FE"));

//		buffer.addLast(new ObdCommand("22 04FE 051A 04FE"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5"));		
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE, R:1"));
		
		buffer.addLast(new ObdCommand("STPX H:18DB33F1, D:01 0B 0C 11, R:2"));
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 1018, R:1"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5"));		
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE, R:1"));
		
//		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE 1018 051A, R:2"));
//		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE 1018 051A, R:2"));		

		buffer.addLast(new QuitCommand());
		
		final Adjustments optional = Adjustments.builder()
				.adaptiveTiming(
						AdaptiveTimeoutPolicy
						.builder()
						.enabled(Boolean.TRUE)
						.checkInterval(5000)
						.commandFrequency(6)
						.build())
				.producerPolicy(ProducerPolicy
						.builder()
						.priorityQueueEnabled(Boolean.TRUE).build())
				.cacheConfig(CachePolicy
						.builder()
						.resultCacheEnabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
				.build();
		
		final CommandLoop executor = new CommandLoop();
		
		Context.apply(it -> {
			it.reset();
			it.register(PidDefinitionRegistry.class, pidRegistry);
			it.register(CodecRegistry.class, CodecRegistry.builder().adjustments(optional).build());
			
			it.register(CodecRegistry.class,
					CodecRegistry
					.builder()
					.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().build())
					.adjustments(optional).build());
			it.register(CommandsBuffer.class, buffer);
			it.register(ConnectionManager.class, new ConnectionManager(connection, Adjustments.DEFAULT));
		});
		
		

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));
		executorService.shutdown();
	}
	
	
	
	
	private PidDefinitionRegistry toPidRegistry(Pids pids) {
		PidDefinitionRegistry pidRegistry = null;
		try (final Resources sources = Resources.convert(pids)) {
			pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
		return pidRegistry;
	}
}
