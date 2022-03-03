package org.obd.metrics;

import java.util.Collection;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.Connector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandExecutor {

	private final CodecRegistry codecRegistry;
	private final Connector connector;
	private final Lifecycle lifecycle;
	private final EventsPublishlisher<Reply<?>> publisher;
	private final PidDefinitionRegistry pids;

	void execute(Command command) {
		connector.transmit(command);
		
		final RawMessage raw = connector.receive();

		if (raw.isNoData()) {
			log.debug("Received no data.");
		} else if (raw.isError()) {
			log.debug("Receive device error: {}", raw);
			lifecycle.onError(raw.getMessage(), null);
		} else if (command instanceof BatchCodec) {
			((BatchCodec) command).decode(null, raw).forEach(this::decodeAndPublishObdMetric);
		} else if (command instanceof ObdCommand) {
			decodeAndPublishObdMetric((ObdCommand) command, raw);
		} else {
			publisher.onNext(Reply.builder().command(command).raw(raw).build());
		}
	}

	private void decodeAndPublishObdMetric(final ObdCommand command,
	        final RawMessage raw) {

		final Codec<?> codec = codecRegistry.findCodec(command);
		final Collection<PidDefinition> allVariants = pids.findAllBy(command.getPid());

		allVariants.forEach(pDef -> {
			Object value = null;
			if (codec != null) {
				value = codec.decode(pDef, raw);
			}

			final ObdMetric metric = ObdMetric.builder()
			        .command(allVariants.size() == 1 ? command : new ObdCommand(pDef)).raw(raw)
			        .value(value).build();

			publisher.onNext(metric);
		});
	}
}
