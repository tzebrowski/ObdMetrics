package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.ConnectorResponseDecoder;
import org.obd.metrics.api.CommandLoop;
import org.obd.metrics.api.Resources;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
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

public class STNxxx_IntegrationTest {

	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.transport.StreamConnector");
		logger.setLevel(Level.TRACE);
				 

		
		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();
		
		final PidDefinitionRegistry pidRegistry = toPidRegistry(pids);

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("D")); // default
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP 6"));
//		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 1924 1003 1827 1828 181F 1002 1956 381A 181D 1862, R:2"));	// 1.75tbi	
		buffer.addLast(new ObdCommand("STPX H:7DF, D:01 0B 0C 0D 04 05 11"));		

		for (int i=0; i<50000; i++) {
			buffer.addLast(new ObdCommand("STPX H:7DF, D:01 0B 0C 0D 04 05 11"));		

		}
		buffer.addLast(new QuitCommand());
		
		final Adjustments optional = Adjustments.builder()
				.adaptiveTimeoutPolicy(
						AdaptiveTimeoutPolicy
						.builder()
						.enabled(Boolean.TRUE)
						.checkInterval(5000)
						.commandFrequency(6)
						.build())
				.producerPolicy(ProducerPolicy
						.builder()
						.priorityQueueEnabled(Boolean.TRUE).build())
				.cachePolicy(CachePolicy
						.builder()
						.resultCacheEnabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
				.build();

		final Callable<Void> executor = new CommandLoop();
		final Callable<Void> commandDecoder = new ConnectorResponseDecoder(optional);

		Context.apply(it -> {
			it.reset();
			it.register(PidDefinitionRegistry.class, pidRegistry);
			it.register(CodecRegistry.class, CodecRegistry.builder().adjustments(optional).build());
			it.register(ConnectorResponseBuffer.class, ConnectorResponseBuffer.instance());
			it.register(CodecRegistry.class,
					CodecRegistry
					.builder()
					.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().build())
					.adjustments(optional).build());
			it.register(CommandsBuffer.class, buffer);
		});
		
		

		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		List<Callable<Void>> aa = new ArrayList<Callable<Void>>();
		aa.add(executor);
		aa.add(commandDecoder);
		executorService.invokeAll(aa);
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
