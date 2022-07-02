package org.obd.metrics.executor;

import org.obd.metrics.Reply;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class QuitCommandExecutor extends CommandExecutor {

	@Override
	public ExecutionStatus execute(ExecutionContext context, Command command) throws InterruptedException {
		log.info("Stopping Command Loop thread. Finishing communication.");
		publishQuitCommand(context);
		context.publisher.onCompleted();
		return ExecutionStatus.ABORT;
	}

	private void publishQuitCommand(ExecutionContext context) {
		context.publisher.onNext(Reply.builder().command(new QuitCommand()).build());
	}
}
