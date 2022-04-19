package org.obd.metrics;

import java.util.Collection;

import org.obd.metrics.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
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
	private final MetricValidator metricValidator = new MetricValidator();

	void execute(Command command) {
		connector.transmit(command);

		final RawMessage message = connector.receive();

		if (message.isEmpty()) {
			log.debug("Received no data");
		} else if (message.isError()) {
			log.error("Receive device error: {}", message.getMessage());
			if (null != lifecycle) {
				lifecycle.onError(message.getMessage(), null);
			}
		} else if (command instanceof BatchObdCommand) {
			final BatchObdCommand batch = (BatchObdCommand) command;
			batch.getCodec().decode(null, message).forEach(this::handle);
		} else if (command instanceof ObdCommand) {
			handle((ObdCommand) command, message);
		} else {
			// release here the message
			publisher.onNext(Reply.builder().command(command).raw(message.getMessage()).build());
		}
	}

	private void handle(final ObdCommand command, final RawMessage raw) {

		final Collection<PidDefinition> allVariants = pids.findAllBy(command.getPid());
		if (allVariants.size() == 1) {
			final ObdMetric metric = ObdMetric.builder()
			        .command(command)
			        .value(decode(command.getPid(), raw)).build();
			validateAndPublish(metric);

		} else {
			allVariants.forEach(pid -> {
				final ObdMetric metric = ObdMetric.builder()
				        .command(new ObdCommand(pid)).raw(raw.getMessage()).value(decode(pid, raw)).build();
				validateAndPublish(metric);
			});
		}
	}

	private Object decode(final PidDefinition pid, final RawMessage raw) {
		final Codec<?> codec = codecRegistry.findCodec(pid);

		Object value = null;
		if (codec != null) {
			value = codec.decode(pid, raw);
		}
		return value;
	}

	private void validateAndPublish(final ObdMetric metric) {
		if (metricValidator.validate(metric) == MetricValidatorStatus.OK) {
			publisher.onNext(metric);
		}
	}
}
