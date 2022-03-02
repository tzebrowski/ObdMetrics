package org.obd.metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.Connector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandExecutor {

	@Default
	private static final List<String> ERRORS = Arrays.asList("UNABLETOCONNECT", "STOPPED", "ERROR", "CANERROR",
	        "BUSINIT");

	private final CodecRegistry codecRegistry;
	private final Connector connector;
	private final Lifecycle lifecycle;
	private final EventsPublishlisher<Reply<?>> publisher;
	private final PidDefinitionRegistry pids;

	void execute(Command command) {
		connector.transmit(command);

		final String data = connector.receive();
		if (null == data || data.contains("nodata")) {
			log.debug("Received no data.");
		} else if (ERRORS.contains(data)) {
			log.debug("Receive device error: {}", data);
			lifecycle.onError(data, null);
		} else if (command instanceof BatchCodec) {
			((BatchCodec) command).decode(null, data).forEach(this::decodeAndPublishObdMetric);
		} else if (command instanceof ObdCommand) {
			decodeAndPublishObdMetric((ObdCommand) command, data);
		} else {
			publisher.onNext(Reply.builder().command(command).raw(data).build());
		}
	}

	private void decodeAndPublishObdMetric(final ObdCommand command,
	        final String data) {

		final Codec<?> codec = codecRegistry.findCodec(command);
		final Collection<PidDefinition> allVariants = pids.findAllBy(command.getPid());

		allVariants.forEach(pDef -> {
			Object value = null;
			if (codec != null) {
				value = codec.decode(pDef, data);
			}

			final ObdMetric metric = ObdMetric.builder()
			        .command(allVariants.size() == 1 ? command : new ObdCommand(pDef)).raw(data)
			        .value(value).build();

			publisher.onNext(metric);
		});
	}
}
