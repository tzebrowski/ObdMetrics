package org.obd.metrics.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ExecutorPolicy;
import org.obd.metrics.MetricsObserver;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsCollector;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Workflow {


	protected final CommandsBuffer buffer = CommandsBuffer.DEFAULT;
	protected final ProducerPolicy policy = ProducerPolicy.DEFAULT;
	protected final ExecutorPolicy executorPolicy = ExecutorPolicy.DEFAULT;

	// just a single thread in a pool
	protected static ExecutorService taskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@Getter
	protected final StatisticsCollector statistics = new StatisticsCollector();

	@Getter
	@NonNull
	protected final PidRegistry pidRegistry;

	protected CodecRegistry codecRegistry;
	protected MetricsObserver metricsObserver;
	protected StatusObserver statusObserver;
	
	
	public abstract void start(Connection connection, Set<String> filter, boolean batchEnabled);

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull String equationEngine, @NonNull MetricsObserver metricsObserver,
			StatusObserver statusObserver, boolean enableStatistics) throws IOException {
		
		final Workflow workflow = new Mode1Workflow();
		workflow.metricsObserver = metricsObserver;
		workflow.codecRegistry = CodecRegistry.builder().equationEngine(equationEngine).pids(workflow.pidRegistry).build();
		workflow.statusObserver = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull MetricsObserver metricsObserver, StatusObserver statusObserver) throws IOException {
	
		final Workflow workflow = new GenericWorkflow(ecuSpecific);
		workflow.metricsObserver = metricsObserver;
		workflow.codecRegistry = CodecRegistry.builder().equationEngine(equationEngine).pids(workflow.pidRegistry).build();
		workflow.statusObserver = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}

	Workflow(String resourceFile) throws IOException {
		try (final InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resourceFile)) {
			this.pidRegistry = PidRegistry.builder().source(stream).build();
		}
	}

	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		buffer.addFirst(new QuitCommand());
		statusObserver.onStopping();
	}
}
