package org.obd.metrics.api.integration.raw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.obd.metrics.api.CommandLoop;
import org.obd.metrics.api.ConnectionManager;
import org.obd.metrics.api.ConnectorResponseDecoder;
import org.obd.metrics.api.Resources;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorPolicy;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

abstract class RawIntegrationRunner {
	
	protected void runBtTest(final Pids pids, final CommandsBuffer buffer, final Adjustments optional)
			throws IOException, InterruptedException {
		runBtTest("000D18000001", pids, buffer, optional);
	}
	
	protected void runBtTest(final String btDeviceName, final Pids pids, final CommandsBuffer buffer, final Adjustments optional)
			throws IOException, InterruptedException {

		final PidDefinitionRegistry pidRegistry = toPidRegistry(pids);

		final AdapterConnection connection = BluetoothConnection.of(btDeviceName);
		final ConnectionManager connectionManager = new ConnectionManager(connection, optional);
		final Callable<Void> decoder = new ConnectorResponseDecoder(optional);
		final Callable<Void> loop = new CommandLoop();

		Context.apply(it -> {
			it.reset();

			it.resolve(Subscription.class).apply(p -> {
				p.subscribe((org.obd.metrics.api.model.Lifecycle) decoder);
				p.subscribe((org.obd.metrics.api.model.Lifecycle) connectionManager);
				p.subscribe((org.obd.metrics.api.model.Lifecycle) loop);
				p.onConnecting();
			});

			it.register(ConnectionManager.class, connectionManager);
			it.register(PidDefinitionRegistry.class, pidRegistry);
			it.register(CodecRegistry.class, CodecRegistry.builder().adjustments(optional).build());
			it.register(ConnectorResponseBuffer.class, ConnectorResponseBuffer.instance());
			it.register(CodecRegistry.class, CodecRegistry.builder()
					.formulaEvaluatorPolicy(FormulaEvaluatorPolicy.builder().build()).adjustments(optional).build());
			it.register(CommandsBuffer.class, buffer);
			
			it.init();
		});

		
		final ExecutorService executorService = Executors.newFixedThreadPool(3);
		List<Callable<Void>> threadsList = new ArrayList<Callable<Void>>();
		threadsList.add(loop);
		threadsList.add(decoder);
		executorService.invokeAll(threadsList);
		executorService.shutdown();
	}

	protected PidDefinitionRegistry toPidRegistry(Pids pids) {
		PidDefinitionRegistry pidRegistry = null;
		try (final Resources sources = Resources.convert(pids)) {
			pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
		return pidRegistry;
	}
}
