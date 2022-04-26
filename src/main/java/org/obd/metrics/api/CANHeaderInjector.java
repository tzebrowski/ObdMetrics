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

	private static final String AT_COMMAND = "AT";
	private final Map<String, String> headers = new HashMap<String, String>();
	private final AtomicBoolean singleModeTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);
	private boolean singleMode = false;
	private final CommandsBuffer buffer;
	private String currentMode;

	CANHeaderInjector(CommandsBuffer buffer, Init init) {
		this.buffer = buffer;
		init.getHeaders().forEach(h -> {
			if (h.getMode() != null && h.getHeader() != null) {
				log.info("Found CAN header= {} for mode = {}", h.getHeader(), h.getMode());
				headers.put(h.getMode(), h.getHeader());
			}
		});
	}

	void testSingleMode(List<ObdCommand> commands) {
		if (singleModeTest.compareAndSet(false, true)) {
			final Map<String, List<ObdCommand>> groupedByMode = commands.stream().filter(p -> !p.getMode()
			        .equals(AT_COMMAND))
			        .collect(Collectors.groupingBy(ObdCommand::getMode));
			if (groupedByMode.size() == 1) {
				singleMode = true;
			}

			log.info("Determined single mode = {}, available modes: {}", singleMode, groupedByMode.keySet());
		}
	}

	void injectHeader(ObdCommand nextCommand) {
		final String nextMode = nextCommand.getMode();
		if (nextMode.equals(AT_COMMAND)) {
			return;
		}

		if (nextMode.equals(currentMode)) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change CAN header, previous header is the same.");
			}
		} else {
			currentMode = nextMode;
			final String nextHeader = headers.get(nextMode);
			log.trace("Setting CAN header={} for the mode to {}", nextHeader, nextMode);
			if (headers.containsKey(nextMode)) {
				if (singleMode) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting CAN header={} for the mode to {}", nextHeader, singleMode);
						buffer.addLast(new ATCommand("SH" + nextHeader));
					}
				} else {
					buffer.addLast(new ATCommand("SH" + nextHeader));
				}
			}
		}
	}
}