package org.obd.metrics.api;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class PIDsGroupHandler {

	static void appendBuffer(Init init, Adjustments adjustements) {
		Context.apply(ctx -> {
			ctx.resolve(PidDefinitionRegistry.class).apply(registry -> {
				adjustements.getRequestedGroups().forEach(group -> {
					log.info("Group: {} is enabled. Adding {} group commands to the queue.", group, group);
					final List<Command> commands = registry.findBy(group).stream()
							.map(p -> mapToCommand(group.getDefaultCommandClass(), p))
							.filter(Optional::isPresent)
							.map(p -> p.get())
							.collect(Collectors.toList());
					final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);
					headerManager.testSingleMode(commands);
					final CommandsBuffer commandsBuffer = ctx.resolve(CommandsBuffer.class).get();

					commands.forEach(command -> {
						headerManager.switchHeader(command);
						commandsBuffer.addLast(command);
					});
				});
			});
		});
	}

	@SuppressWarnings("unchecked")
	static private Optional<Command> mapToCommand(Class<?> defaultClass, PidDefinition pid) {
		try {
			final Class<?> commandClass = (pid.getCommandClass() == null) ? defaultClass
					: Class.forName(pid.getCommandClass());
			if (commandClass == null) {
				return Optional.empty();
			}
			final Constructor<? extends Command> constructor = (Constructor<? extends Command>) commandClass
					.getConstructor(PidDefinition.class);
			return Optional.of(constructor.newInstance(pid));
		} catch (Throwable e) {
			log.error("Failed to initiate command class: {}", pid.getCommandClass(), e);
		}
		return Optional.empty();
	}
}
