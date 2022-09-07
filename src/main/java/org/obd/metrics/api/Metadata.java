package org.obd.metrics.api;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.MetadataCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Metadata {
	
	void updateBuffer(Init init, CommandsBuffer commandsBuffer,PidDefinitionRegistry pidDefinitionRegistry) {
		log.info("Fetch Metadata is enabled. Adding Metadata commands to the queue.");

		final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);
		final List<Command> metadata = pidDefinitionRegistry
				.findBy(PidType.METADATA)
				.stream()
				.map(this::mapToCommand)
				.collect(Collectors.toList());

		headerManager.testSingleMode(metadata);
		metadata.forEach(command -> {
			headerManager.switchHeader(command);
			commandsBuffer.addLast(command);
		});
	}
	
	@SuppressWarnings("unchecked")
	private Command mapToCommand(PidDefinition p) {

		try {
			final Class<?> clazz = (p.getCommandClass() == null) ? MetadataCommand.class :  
				Class.forName(p.getCommandClass());
			
			final Constructor<? extends Command> constructor = (Constructor<? extends Command>) clazz
					.getConstructor(PidDefinition.class);
			return constructor.newInstance(p);
		} catch (Throwable e) {
			log.error("Failed to initiate command class: {}", p.getCommandClass(), e);
		}
		return null;
	}
}
