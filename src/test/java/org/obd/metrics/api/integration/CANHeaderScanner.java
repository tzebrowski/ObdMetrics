package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CANHeaderScanner {
	String header = "";

	@Test
	public void scan() throws IOException, InterruptedException, ExecutionException {
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
			buffer.add(DefaultCommandGroup.INIT);

			// Create an instance of CodecRegistry that will handle decoding incoming raw
			// OBD frames
			CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();

			// Connection for an OBD adapter
			AdapterConnection connection = BluetoothConnection.openConnection();

			// commandLoop that glue all the ingredients
			CommandLoop commandLoop = CommandLoop
			        .builder()
			        .lifecycle(new Lifecycle() {
				        @Override
				        public void onError(String message, Throwable e) {
					        log.error("Received an error from the device: {}", message);
					        System.exit(-1);
				        }
			        })
			        .connection(connection)
			        .buffer(buffer)
			        .pids(pidRegistry)
			        .observer(new ReplyObserver<Reply<?>>() {

				        @Override
				        public void onNext(Reply<?> t) {
					        if (t instanceof ObdMetric) {
						        log.info("header={}, metrics={}", header, t);

						        System.exit(1);
					        }
				        }
			        })
			        .codecs(codecRegistry)
			        .build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			final Callable<String> producer = () -> {

				Thread.sleep(7000);

				log.info("Start producing headers");
				final ObdCommand command = new ObdCommand(pidRegistry.findBy(6l));

				for (int x = 0; x <= 15; x++)
					for (int j = 0; j <= 15; j++) {
						for (int i = 0; i <= 15; i++) {
							header = String.format("DA1%X%X%X", x, j, i);
							if (header.endsWith("A0")) {
								j++;
								continue;
							}
							if (false) {
								try {
									final int headerNum = Integer.parseInt(header);
									if (headerNum >= 280 && headerNum <= 300) {
										continue;
									}
								} catch (Throwable e) {
								}
							}
							log.error("Checking header: AT SH {}", header);
							buffer.addLast(new ATCommand("SH " + header));
							try {
								Thread.sleep(150);
								buffer.addLast(command);
								Thread.sleep(150);
							} catch (InterruptedException e) {
								log.error(e.getMessage(), e);
							}

						}
					}

				executorService.shutdown();
				return null;
			};

			executorService.invokeAll(Arrays.asList(commandLoop, producer));
		}
	}
}
