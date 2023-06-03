package org.obd.metrics.executor;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class QuitCommandHandler implements CommandHandler {

	@SuppressWarnings("unchecked")
	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			log.info("Stopping Command Loop thread. Finishing communication.");
			p.onNext(Reply.builder().command(new QuitCommand()).build());
			p.onCompleted();
		});

		return CommandExecutionStatus.ABORT;
	}
}
