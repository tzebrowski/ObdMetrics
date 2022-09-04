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
final class DevicePropertiesReader extends ReplyObserver<Reply<?>> {

	@Getter
	private final Map<String, String> properties = new HashMap<>();

	@Override
	public void onNext(Reply<?> reply) {
		final MetadataCommand deviceProperty = (MetadataCommand) reply.getCommand();
		log.debug("Recieved device property: {}", reply);

		if (deviceProperty instanceof Codec<?>) {
			final Object decode = ((Codec<?>) deviceProperty).decode(null, reply.getRaw());
			if (decode == null) {
				properties.put(deviceProperty.getLabel(), reply.getRaw().getMessage());
			} else {
				properties.put(deviceProperty.getLabel(), decode.toString());
			}
		} else {
			properties.put(deviceProperty.getLabel(), reply.getRaw().getMessage());
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(MetadataCommand.class);
	}
}
