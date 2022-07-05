package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private final PidDefinitionRegistry pidRegistry;

	private ReplyObserver<Reply<?>> externalEventsObserver;
	private final String equationEngine;

	private final Lifecycle lifecycle;
	
	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(Pids pids, String equationEngine, ReplyObserver<Reply<?>> eventsObserver,
			Lifecycle lifecycle) {

		log.info("Creating an instance of the '{}' workflow", getClass().getSimpleName());
		this.equationEngine = equationEngine;
		this.externalEventsObserver = eventsObserver;
		this.lifecycle = lifecycle;
		
		try (final Resources sources = Resources.convert(pids)) {
			this.pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
	}

	@Override
	public void stop() {

		Context.apply(context -> {
			context.resolve(CommandsBuffer.class).apply(commandsBuffer -> {
				log.info("Stopping the workflow. Publishing QUIT command...");
				commandsBuffer.clear();
				commandsBuffer.addFirst(new QuitCommand());
			});

			context.resolve(Subscription.class).apply(p -> {
				log.info("Publishing lifecycle changes");
				p.onStopping();
			});
		});

	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
			@NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {

				log.info("Starting the workflow. Protocol: {}, headers: {}, adjustements: {}, selected PID's: {}",
						init.getProtocol(), init.getHeaders(), adjustements, query.getPids());
				
				Context.apply(it -> {
					it.reset();
					it.register(Subscription.class, new Subscription()).apply(p -> {
						p.subscribe(lifecycle);
					});
				});
				
				initCodecRegistry(adjustements);
				initCommandBuffer(init);
				
				
				final CommandProducer commandProducerThread = buildCommandProducer(adjustements,
						getCommandsSupplier(adjustements, query), init);

				Context.apply(it -> {
					it.resolve(Subscription.class).apply(p -> {
						p.subscribe(commandProducerThread);
						p.onConnecting();
					});

					it.register(EventsPublishlisher.class, EventsPublishlisher.builder()
							.observer(externalEventsObserver).observer((ReplyObserver<Reply<?>>) diagnostics).build());

					it.register(PidDefinitionRegistry.class, pidRegistry);
				});
				
				diagnostics.reset();

				executorService.invokeAll(Arrays.asList(new CommandLoop(connection), commandProducerThread));

			} catch (Throwable e) {
				log.error("Failed to initialize the framework.", e);
			} finally {
				log.info("Stopping the Workflow.");

				Context.instance().resolve(Subscription.class).apply(p -> {
					p.onStopped();
				});

				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	private CommandProducer buildCommandProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier,
			Init init) {
		return new CommandProducer(diagnostics, supplier, adjustements, init);
	}

	private void initCodecRegistry(Adjustments adjustments) {
		Context.instance()
				.register(CodecRegistry.class,
						CodecRegistry.builder().equationEngine(equationEngine).adjustments(adjustments).build())
				.apply(codecRegistry -> {
					DefaultCommandGroup.SUPPORTED_PIDS.getCommands().forEach(p -> {
						codecRegistry.register(p.getPid(), p);
					});
				});
	}

	private void initCommandBuffer(Init init) {

		Context.instance().register(CommandsBuffer.class, CommandsBuffer.instance()).apply(commandsBuffer -> {
			commandsBuffer.clear();
			commandsBuffer.add(init.getSequence());

			if (init.isFetchDeviceProperties()) {
				log.info("Add commands to the queue fetch devices properties.");
				commandsBuffer.add(DefaultCommandGroup.DEVICE_PROPERTIES);
			}

			// Protocol
			commandsBuffer.addLast(new ATCommand("SP" + init.getProtocol().getType()));
			if (init.isFetchSupportedPids()) {
				log.info("Add commands to the queue to fetch supported PIDs.");
				commandsBuffer.add(DefaultCommandGroup.SUPPORTED_PIDS);
			}

			commandsBuffer.addLast(new DelayCommand(init.getDelay()));
			commandsBuffer.addLast(new InitCompletedCommand());
		});
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Adjustments adjustements, Query query) {
		return new CommandsSuplier(pidRegistry, adjustements.isBatchEnabled(), query);
	}
}
