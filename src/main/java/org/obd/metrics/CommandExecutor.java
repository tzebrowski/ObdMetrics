package org.obd.metrics;

import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import rx.subjects.PublishSubject;

@Slf4j
@Builder
final class CommandExecutor {

	private static final String NO_DATA = "no data";
	private static final String STOPPED = "stopped";
	private static final String UNABLE_TO_CONNECT = "unable to connect";

	private CodecRegistry codecRegistry;
	private Connections connections;
	private StatusObserver statusObserver;
	private PublishSubject<Metric<?>> publisher;

	void execute(Command command) {
		
		var data = connections.transmit(command).receive();
		if (null == data || data.length() == 0) {
			log.debug("Recieved no data.");
		} else if (data.contains(STOPPED)) {
			statusObserver.onError(data, null);
		} else if (data.contains(UNABLE_TO_CONNECT)) {
			statusObserver.onError(data, null);
		} else if (data.contains(NO_DATA)) {
			log.debug("Recieved no data.");
		} else if (command instanceof Batchable) {
			((Batchable) command).decode(data).forEach(this::decodeAndPublish);
		} else if (command instanceof ObdCommand) {
			decodeAndPublish(command, data);
		} else {
			publisher.onNext(Metric.builder().command(command).raw(data).build());
		}
	}

	private void decodeAndPublish(final Command command, final String data) {
		var decoded = codecRegistry.findCodec(command).map(p -> p.decode(data)).orElse(null);
		var metric = Metric.builder().command(command).raw(data).value(decoded).build();
		publisher.onNext(metric);
	}
}
