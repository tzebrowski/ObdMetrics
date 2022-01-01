package org.obd.metrics;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.DeviceProperty;
import org.obd.metrics.command.VinCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DevicePropertiesReader extends ReplyObserver<Reply<?>> {

	@Getter
	private final Map<String, String> properties = new HashMap<>();

	@Override
	public void onNext(Reply<?> reply) {
		final DeviceProperty deviceProperty = (DeviceProperty) reply.getCommand();
		log.debug("Recieved device property: {}", reply);

		if (deviceProperty instanceof Codec<?>) {
			final Object decode = ((Codec<?>) deviceProperty).decode(null, reply.getRaw());
			if (decode == null) {
				properties.put(deviceProperty.getLabel(), reply.getRaw());
			} else {
				properties.put(deviceProperty.getLabel(), decode.toString());
			}
		} else {
			properties.put(deviceProperty.getLabel(), reply.getRaw());
		}
	}

	@Override
	public String[] observables() {
		return new String[] {
		        DeviceProperty.class.getName(),
		        VinCommand.class.getName()
		};
	}
}
