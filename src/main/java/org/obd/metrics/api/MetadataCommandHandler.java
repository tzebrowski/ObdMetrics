package org.obd.metrics.api;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MetadataCommandHandler {

	private static final Class<HexCommand> DEFAULT_CLASS = HexCommand.class;

	void updateBuffer(Init init) {
		log.info("Fetch Metadata is enabled. Adding Metadata commands to the queue.");

		final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);

		Context.instance().resolve(PidDefinitionRegistry.class).apply(registry -> {

			final List<Command> commands = registry.findBy(PidType.METADATA).stream()
					.map(this::mapToCommand).filter(p -> p != null).collect(Collectors.toList());

			headerManager.testSingleMode(commands);
			final CommandsBuffer commandsBuffer = Context.instance().resolve(CommandsBuffer.class).get();

			commands.forEach(command -> {
				headerManager.switchHeader(command);
				commandsBuffer.addLast(command);
			});
		});
	}

	@SuppressWarnings("unchecked")
	private Command mapToCommand(PidDefinition p) {

		try {
			final Class<?> clazz = (p.getCommandClass() == null) ? DEFAULT_CLASS : Class.forName(p.getCommandClass());

			final Constructor<? extends Command> constructor = (Constructor<? extends Command>) clazz
					.getConstructor(PidDefinition.class);
			return constructor.newInstance(p);
		} catch (Throwable e) {
			log.error("Failed to initiate command class: {}", p.getCommandClass(), e);
		}
		return null;
	}
}
