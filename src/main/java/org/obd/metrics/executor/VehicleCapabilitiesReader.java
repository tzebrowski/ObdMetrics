package org.obd.metrics.executor;

import java.util.HashSet;
import java.util.Set;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.pid.PIDsGroup;

final class VehicleCapabilitiesReader extends PIDsGroupReader<Set<String>> {

	VehicleCapabilitiesReader() {
		super(PIDsGroup.CAPABILITES);
		value = new HashSet<String>();
	}

	@Override
	public void onNext(Reply<?> reply) {
		final SupportedPIDsCommand command = (SupportedPIDsCommand) reply.getCommand();
		
		value.addAll(command.decode(command.getPid(), reply.getRaw()));
	}
}
