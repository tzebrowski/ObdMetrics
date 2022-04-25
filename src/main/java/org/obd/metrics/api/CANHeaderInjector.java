package org.obd.metrics.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CANHeaderInjector {
	private final Map<String, String> headers = new HashMap<String, String>();
	private final AtomicBoolean singleModeCheck = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeader = new AtomicBoolean(false);
	private boolean singleMode = false;
	private final CommandsBuffer buffer;

	CANHeaderInjector(CommandsBuffer buffer, Init init) {
		this.buffer = buffer;
		init.getHeaders().forEach(h -> {
			if (h.getMode() != null && h.getHeader() != null) {
				log.info("Found mode = {} and header = {}", h.getMode(), h.getHeader());
				headers.put(h.getMode(), h.getHeader());
			}
		});
	}

	void determineSingleMode(List<ObdCommand> commands) {
		if (singleModeCheck.compareAndSet(false, true)) {
			final Map<String, List<ObdCommand>> groupedByMode = commands.stream().filter(p -> !p.getMode().equals("AT"))
			        .collect(Collectors.groupingBy(ObdCommand::getMode));
			if (groupedByMode.size() == 1) {
				singleMode = true;
			}

			log.info("Determined single mode = {}, available modes: {}", singleMode, groupedByMode.keySet());
		}
	}

	void injectHeader(ObdCommand command) {
		final String mode = command.getMode();
		if (headers.containsKey(mode)) {
			if (singleMode) {
				if (addedSingleModeHeader.compareAndSet(false, true)) {
					final String header = headers.get(mode);
					log.info("Injecting single mode CAN header = {}", singleMode);
					buffer.addLast(new ATCommand("SH" + header));
				}
			} else {
				buffer.addLast(new ATCommand("SH" + headers.get(mode)));
			}
		}
	}
}