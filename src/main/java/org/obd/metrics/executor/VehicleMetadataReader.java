package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PIDsGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class VehicleMetadataReader extends PIDsGroupReader<Map<String, String>>  {

	VehicleMetadataReader() {
		super(PIDsGroup.METADATA);
		value = new HashMap<String, String>();
	}
	
	@Override
	public void onNext(Reply<?> reply) {
		final Command command = (Command) reply.getCommand();
		log.debug("Recieved vehicle metadata: {}", reply);
		
		if (command instanceof Codec<?>) {
			final Object decode = ((Codec<?>) command).decode(reply.getRaw());
			if (decode == null) {
				value.put(command.getLabel(), reply.getRaw().getMessage());
			} else {
				value.put(command.getLabel(), decode.toString());
			}
		} else {
			value.put(command.getLabel(), reply.getRaw().getMessage());
		}
	}
}
