package org.openobd2.core.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ExecutorPolicy;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.StatusObserver;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.pid.PidRegistry;

import lombok.NonNull;


abstract class WorkflowBase implements Workflow {

	protected final CommandsBuffer buffer = CommandsBuffer.instance(); 
	protected final ProducerPolicy policy = ProducerPolicy
			.builder()
			.delayBeforeInsertingCommands(50)
			.emptyBufferSleepTime(200)
			.build();
	
	protected final ExecutorPolicy executorPolicy = ExecutorPolicy.builder().frequency(100).build();

	// just a single thread in a pool
	protected static ExecutorService taskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@NonNull
	protected final PidRegistry pidRegistry;

	@NonNull
	protected final CodecRegistry codecRegistry;
	
	@NonNull
	protected final CommandReplySubscriber subscriber;

	@NonNull
	protected final StatusObserver statusObserver;
	
	WorkflowBase(String equationEngine, CommandReplySubscriber subscriber, StatusObserver statusListener, String resourceFile) 
			throws IOException{
		
		this.subscriber = subscriber;
		this.statusObserver = statusListener == null ? StatusObserver.DUMMY : statusListener;
		
		try(final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFile)){
			this.pidRegistry = PidRegistry.builder().source(stream).build();
		}
		this.codecRegistry = CodecRegistry.builder().equationEngine(equationEngine).pids(this.pidRegistry).build();
	}
}
