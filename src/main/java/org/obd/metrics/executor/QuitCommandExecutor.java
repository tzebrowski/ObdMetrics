package org.obd.metrics.executor;

import org.obd.metrics.api.Context;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class QuitCommandExecutor extends CommandExecutor {

	@SuppressWarnings("unchecked")
	@Override
	public CommandExecutionStatus execute(Command command) throws InterruptedException {

		Context.instance().lookup(EventsPublishlisher.class).ifPresent(p -> {
			log.info("Stopping Command Loop thread. Finishing communication.");
			p.onNext(Reply.builder().command(new QuitCommand()).build());
			p.onCompleted();
		});
		
		return CommandExecutionStatus.ABORT;
	}
}
