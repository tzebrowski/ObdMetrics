package org.obd.metrics.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CANMessageHeaderInjector {

	private static final String AT_COMMAND = "AT";
	private final Map<String, String> canHeaders = new HashMap<String, String>();
	private final AtomicBoolean singleModeTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);
	private boolean isSingleMode = false;
	private String currentMode;
	private final CommandsBuffer buffer;
	
	CANMessageHeaderInjector(Init init) {
		
		init.getHeaders().forEach(h -> {
			if (h.getMode() != null && h.getHeader() != null) {
				log.info("Found CAN header= {} for mode = {}", h.getHeader(), h.getMode());
				canHeaders.put(h.getMode(), h.getHeader());
			}
		});
		
		buffer = Context.instance().lookup(CommandsBuffer.class).get();
	}

	void testSingleMode(List<ObdCommand> commands) {
		if (singleModeTest.compareAndSet(false, true)) {
			final Map<String, List<ObdCommand>> groupedByMode = commands.stream().filter(p -> !p.getMode()
			        .equals(AT_COMMAND))
			        .collect(Collectors.groupingBy(ObdCommand::getMode));
			if (groupedByMode.size() == 1) {
				isSingleMode = true;
			}

			log.info("Determined single mode = {}, available modes: {}", isSingleMode, groupedByMode.keySet());
		}
	}

	void switchHeader(ObdCommand nextCommand) {
		final String nextMode = nextCommand.getMode();

		if (nextMode.equals(AT_COMMAND)) {
			return;
		}

		if (nextMode.equals(currentMode)) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change CAN message header, previous header is the same.");
			}
		} else {
			currentMode = nextMode;
			final String nextHeader = canHeaders.get(nextMode);

			if (log.isTraceEnabled()) {
				log.trace("Setting CAN message header={} for the mode to {}", nextHeader, nextMode);
			}

			if (canHeaders.containsKey(nextMode)) {
				
				
				if (isSingleMode) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting CAN message header={} for the mode to {}", nextHeader, isSingleMode);
						buffer.addLast(prepareCANMessageHeader(nextHeader));
					}
				} else {
					buffer.addLast(prepareCANMessageHeader(nextHeader));
				}
			}
		}
	}

	private ATCommand prepareCANMessageHeader(final String nextHeader) {
		return new ATCommand("SH" + nextHeader);
	}
}