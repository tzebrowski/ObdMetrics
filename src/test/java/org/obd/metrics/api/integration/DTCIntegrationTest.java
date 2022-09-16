package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.CommandLoop;
import org.obd.metrics.api.Resources;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CacheConfig;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class DTCIntegrationTest {

	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.transport.DefaultConnector");
		logger.setLevel(Level.TRACE);
				 
//		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);
		final AdapterConnection connection = BluetoothConnection.openConnection();

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");

		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json")).build();
		
		final PidDefinitionRegistry pidRegistry = toPidRegistry(pids);

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("PP 2CSV 01"));
		buffer.addLast(new ATCommand("PP 2C ON"));
		buffer.addLast(new ATCommand("PP 2DSV 01"));
		buffer.addLast(new ATCommand("PP 2D ON"));
		buffer.addLast(new ATCommand("S 7"));
		
		
		buffer.addLast(new ATCommand("SH DB33F1"));
		
		
		buffer.addLast(new ObdCommand("01 05"));
		
		buffer.addLast(new ObdCommand("03"));
		buffer.addLast(new ObdCommand("02"));
		buffer.addLast(new ObdCommand("05"));
		buffer.addLast(new ObdCommand("06"));
		buffer.addLast(new ObdCommand("07"));
		buffer.addLast(new ObdCommand("08"));

		buffer.addLast(new ATCommand("SH DA10F1"));

		buffer.addLast(new ObdCommand("19020D"));

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
				.cacheConfig(CacheConfig
						.builder()
						.resultCacheEnabled(Boolean.FALSE).build())
				.batchEnabled(true)
				.build();

		final CommandLoop executor = new CommandLoop(connection);
		
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
		});
		
		

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));
		executorService.shutdown();
		source.close();
	}
	
	private PidDefinitionRegistry toPidRegistry(Pids pids) {
		PidDefinitionRegistry pidRegistry = null;
		try (final Resources sources = Resources.convert(pids)) {
			pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}

		for (final ObdCommand p : DefaultCommandGroup.SUPPORTED_PIDS.getCommands()) {
			pidRegistry.register(p.getPid());
		}

		return pidRegistry;
	}
}
