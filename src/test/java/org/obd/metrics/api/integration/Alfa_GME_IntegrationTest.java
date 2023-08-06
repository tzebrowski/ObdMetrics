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
import org.obd.metrics.api.model.ErrorsPolicy;
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

public class Alfa_GME_IntegrationTest {

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

		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:6"));		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:7"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:9"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004"));

		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:5"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:6"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:7"));
			
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA"));

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
