package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	protected final InitConfiguration initConfiguration;
	protected Pids pidConfiguration;
	protected CommandProducer commandProducer;
	protected final CommandsBuffer commandsBuffer = CommandsBuffer.instance();

	@Getter
	protected Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	protected final PidDefinitionRegistry pidRegistry;

	protected CodecRegistry codecRegistry;
	protected ReplyObserver<Reply<?>> replyObserver;
	protected final String equationEngine;
	protected final Lifecycle.Subscription subscription = Lifecycle.subscription;
	protected final Lifecycle toSubscibe;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(InitConfiguration init,
	        Pids pids,
	        String equationEngine,
	        ReplyObserver<Reply<?>> observer,
	        Lifecycle lifecycle) {

		log.info("Creating an instance of the '{}' workflow", getClass().getSimpleName());
		this.initConfiguration = init;
		this.pidConfiguration = pids;
		this.equationEngine = equationEngine;
		this.replyObserver = observer;
		this.toSubscibe = lifecycle;

		try (final Resources sources = Resources.convert(pids)) {
			this.pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		commandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		subscription.onStopping();
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {

				initLifecycleSubscription();

				codecRegistry = getCodecRegistry(adjustements);

				init();
				initCommandBuffer();

				log.info("Starting the workflow. Protocol: {}, header: {}, adjustements: {}, selected PID's: {}",
						initConfiguration.getProtocol(), initConfiguration.getHeader(), adjustements, query.getPids());

				diagnostics.reset();

				commandProducer = getProducer(adjustements, getCommandsSupplier(adjustements,
				        query));

				@SuppressWarnings("unchecked")
				final CommandLoop commandLoop = CommandLoop
				        .builder()
				        .connection(connection)
				        .buffer(commandsBuffer)
				        .observers(getObservers())
				        .observer(replyObserver)
				        .observer((ReplyObserver<Reply<?>>) diagnostics)
				        .pids(pidRegistry)
				        .codecs(codecRegistry)
				        .lifecycle(subscription).build();

				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer));

			} catch (Throwable e) {
				log.error("Failed to initialize the framework.", e);
			} finally {
				log.info("Stopping the Workflow.");
				subscription.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	protected CommandProducer getProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier) {
		return new CommandProducer(diagnostics, commandsBuffer, supplier, adjustements);
	}

	protected CodecRegistry getCodecRegistry(Adjustments adjustments) {
		return CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine)).adjustments(adjustments)
		        .build();
	}

	protected @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}

	private List<ReplyObserver<Reply<?>>> getObservers() {
		return Arrays.asList(commandProducer);
	}

	private void initLifecycleSubscription() {
		subscription.unregisterAll();
		subscription.subscribe(toSubscibe);
	}

	private void init() {
		subscription.onConnecting();
		commandsBuffer.clear();

		DefaultCommandGroup.SUPPORTED_PIDS.getCommands().forEach(p -> {
			codecRegistry.register(p.getPid(), p);
		});
	}

	private void initCommandBuffer() {
		commandsBuffer.add(initConfiguration.getSequence());
		commandsBuffer.addLast(new ATCommand("SP" + initConfiguration.getProtocol().getType()));

		if (initConfiguration.getHeader() != null && initConfiguration.getHeader().length() > 0) {
			commandsBuffer.addLast(new ATCommand("SH" + initConfiguration.getHeader()));
		}

		commandsBuffer.add(DefaultCommandGroup.SUPPORTED_PIDS);
		commandsBuffer.addLast(new DelayCommand(initConfiguration.getDelay()));
		commandsBuffer.addLast(new InitCompletedCommand());
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Adjustments adjustements, Query query) {
		return new CommandsSuplier(pidRegistry, adjustements.isBatchEnabled(),
		        query);
	}
}
