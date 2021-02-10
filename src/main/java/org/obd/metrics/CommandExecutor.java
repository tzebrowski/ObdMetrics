package org.obd.metrics;

import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.Connections;
import org.obd.metrics.pid.PidRegistry;

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
	private PublishSubject<Reply> publisher;
	private PidRegistry pids;

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
			((Batchable) command).decode(data).forEach(this::decodeAndPublishObdMetric);
		} else if (command instanceof ObdCommand) {
			decodeAndPublishObdMetric((ObdCommand) command, data);
		} else {
			publisher.onNext(Reply.builder().command(command).raw(data).build());
		}
	}

	private void decodeAndPublishObdMetric(final ObdCommand command, final String data) {

		var codec = codecRegistry.findCodec(command);
		var findAllBy = pids.findAllBy(command.getPid().getPid());
		findAllBy.forEach(pDef -> {
			var value = codec.map(p -> p.decode(pDef, data)).orElse(null);
			var metric = ObdMetric.builder().command(findAllBy.size() == 1 ? command : new ObdCommand(pDef)).raw(data)
					.value(value).build();
			publisher.onNext(metric);
		});
	}

}
