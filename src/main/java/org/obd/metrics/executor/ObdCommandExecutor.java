package org.obd.metrics.executor;

import java.util.Collection;

import org.obd.metrics.api.ObdMetric;
import org.obd.metrics.api.Reply;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class ObdCommandExecutor extends CommandExecutor {

	@Override
	public CommandExecutionStatus execute(ExecutionContext context, Command command) {
		context.connector.transmit(command);

		final RawMessage message = context.connector.receive();

		if (message.isEmpty()) {
			log.debug("Received no data");
		} else if (message.isError()) {
			log.error("Receive device error: {}", message.getMessage());
			if (null != context.lifecycle) {
				context.lifecycle.onError(message.getMessage(), null);
			}
		} else if (command instanceof BatchObdCommand) {
			final BatchObdCommand batch = (BatchObdCommand) command;
			batch.getCodec().decode(null, message).forEach((a, b) -> handle(context, a, b));
		} else if (command instanceof ObdCommand) {
			handle(context, (ObdCommand) command, message);
		} else {
			// release here the message
			context.publisher.onNext(Reply.builder().command(command).raw(message.getMessage()).build());
		}
		return CommandExecutionStatus.OK;
	}

	private void handle(final ExecutionContext context, final ObdCommand command, final RawMessage raw) {

		final Collection<PidDefinition> allVariants = context.pids.findAllBy(command.getPid());
		if (allVariants.size() == 1) {
			final ObdMetric metric = ObdMetric.builder().command(command).value(decode(context, command.getPid(), raw))
					.build();
			validateAndPublish(context, metric);

		} else {
			allVariants.forEach(pid -> {
				final ObdMetric metric = ObdMetric.builder().command(new ObdCommand(pid)).raw(raw.getMessage())
						.value(decode(context, pid, raw)).build();
				validateAndPublish(context, metric);
			});
		}
	}

	private Object decode(final ExecutionContext context, final PidDefinition pid, final RawMessage raw) {
		final Codec<?> codec = context.codecRegistry.findCodec(pid);

		Object value = null;
		if (codec != null) {
			value = codec.decode(pid, raw);
		}
		return value;
	}

	private void validateAndPublish(final ExecutionContext context, final ObdMetric metric) {
		if (context.metricValidator.validate(metric) == MetricValidatorStatus.OK) {
			context.publisher.onNext(metric);
		}
	}
}
