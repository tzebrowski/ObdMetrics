package org.obd.metrics.executor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.MetadataCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class VehicleMetadataReader extends ReplyObserver<Reply<?>> {

	@Getter
	private final Map<String, String> metadata = new HashMap<>();

	@Override
	public void onNext(Reply<?> reply) {
		final MetadataCommand metadataCommand = (MetadataCommand) reply.getCommand();
		log.debug("Recieved vehicle metadata: {}", reply);

		if (metadataCommand instanceof Codec<?>) {
			final Object decode = ((Codec<?>) metadataCommand).decode(null, reply.getRaw());
			if (decode == null) {
				metadata.put(metadataCommand.getLabel(), reply.getRaw().getMessage());
			} else {
				metadata.put(metadataCommand.getLabel(), decode.toString());
			}
		} else {
			metadata.put(metadataCommand.getLabel(), reply.getRaw().getMessage());
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(MetadataCommand.class);
	}
}
