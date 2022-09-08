package org.obd.metrics.executor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.command.meta.NotEncodedCommand;
import org.obd.metrics.command.meta.TimeCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class VehicleMetadataReader extends ReplyObserver<Reply<?>> {

	@Getter
	private final Map<String, String> metadata = new HashMap<>();

	@Override
	public void onNext(Reply<?> reply) {
		final Command command = (Command) reply.getCommand();
		log.debug("Recieved vehicle metadata: {}", reply);

		if (command instanceof Codec<?>) {
			final Object decode = ((Codec<?>) command).decode(null, reply.getRaw());
			if (decode == null) {
				metadata.put(command.getLabel(), reply.getRaw().getMessage());
			} else {
				metadata.put(command.getLabel(), decode.toString());
			}
		} else {
			metadata.put(command.getLabel(), reply.getRaw().getMessage());
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(HexCommand.class, TimeCommand.class, NotEncodedCommand.class);
	}
}
