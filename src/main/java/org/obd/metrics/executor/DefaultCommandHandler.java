package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.buffer.decoder.ResponseBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultCommandHandler implements CommandHandler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final Map<Class<? extends Command>, ? extends CommandHandler> registry = new HashMap() {
		private static final long serialVersionUID = 6536620581251911405L;
		{
			put(DelayCommand.class, new DelayCommandHandler());
			put(InitCompletedCommand.class, new InitCompletedHandler());
			put(QuitCommand.class, new QuitCommandHandler());
		}
	};

	private final CommandHandler fallback;

	DefaultCommandHandler(ResponseBuffer responseBuffer) {
		this.fallback = new ObdCommandHandler(responseBuffer);
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws Exception {
		log.trace("Executing the command: {}", command);
		return findHandler(command).execute(connector, command);
	}

	private CommandHandler findHandler(Command command) {
		CommandHandler handler = null;
		if (registry.containsKey(command.getClass())) {
			handler = registry.get(command.getClass());
		} else {
			handler = fallback;
		}
		return handler;
	}
}