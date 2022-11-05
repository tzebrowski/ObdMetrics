package org.obd.metrics.executor;

import java.util.Collection;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class ObdCommandExecutor implements CommandExecutor {

	@SuppressWarnings("unchecked")
	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		connector.transmit(command);
		final ConnectorResponse message = connector.receive();

		if (message.isEmpty()) {
			log.debug("Received no data");
		} else if (message.isError()) {
			log.error("Receive device error: {}", message.getMessage());

			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onError(message.getMessage(), null);
			});

		} else if (command instanceof BatchObdCommand) {
			final BatchObdCommand batch = (BatchObdCommand) command;
			batch.getCodec().decode(null, message).forEach(this::handle);
		} else if (command instanceof ObdCommand) {
			handle((ObdCommand) command, message);
		} else {
			Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
				// release here the message
				p.onNext(Reply.builder().command(command).raw(message).build());
			});
		}
		return CommandExecutionStatus.OK;
	}

	private void handle(final ObdCommand command, final ConnectorResponse raw) {
		final PidDefinitionRegistry pids = Context.instance().resolve(PidDefinitionRegistry.class).get();

		final Collection<PidDefinition> allVariants = pids.findAllBy(command.getPid());
		if (allVariants.size() == 1) {
			final ObdMetric metric = ObdMetric.builder().command(command).value(decode(command.getPid(), raw)).raw(raw).build();
			validateAndPublish(metric);

		} else {
			allVariants.forEach(pid -> {
				final ObdMetric metric = ObdMetric.builder().command(new ObdCommand(pid)).raw(raw)
						.value(decode(pid, raw)).build();
				validateAndPublish(metric);
			});
		}
	}

	private Object decode(final PidDefinition pid, final ConnectorResponse raw) {
		final CodecRegistry codecRegistry = Context.instance().resolve(CodecRegistry.class).get();

		final Codec<?> codec = codecRegistry.findCodec(pid);

		Object value = null;
		if (codec != null) {
			value = codec.decode(pid, raw);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private void validateAndPublish(final ObdMetric metric) {
		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			final MetricValidator metricValidator = new MetricValidator();
			if (metricValidator.validate(metric) == MetricValidatorStatus.OK) {
				p.onNext(metric);
			}
		});
	}
}
