package org.obd.metrics.api;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandHandler {

	void updateBuffer(PidType pidType, Class<?> defaultClass, Init init) {
		final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);

		Context.instance().resolve(PidDefinitionRegistry.class).apply(registry -> {

			final List<Command> commands = registry.findBy(pidType).stream().map(p -> mapToCommand(defaultClass, p))
					.filter(p -> p != null).collect(Collectors.toList());

			headerManager.testSingleMode(commands);
			final CommandsBuffer commandsBuffer = Context.instance().resolve(CommandsBuffer.class).get();

			commands.forEach(command -> {
				headerManager.switchHeader(command);
				commandsBuffer.addLast(command);
			});
		});
	}

	@SuppressWarnings("unchecked")
	private Command mapToCommand(Class<?> defaultClass, PidDefinition p) {

		try {
			final Class<?> clazz = (p.getCommandClass() == null) ? defaultClass : Class.forName(p.getCommandClass());

			final Constructor<? extends Command> constructor = (Constructor<? extends Command>) clazz
					.getConstructor(PidDefinition.class);
			return constructor.newInstance(p);
		} catch (Throwable e) {
			log.error("Failed to initiate command class: {}", p.getCommandClass(), e);
		}
		return null;
	}
}
