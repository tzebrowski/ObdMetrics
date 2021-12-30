package org.obd.metrics;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.DeviceProperty;
import org.obd.metrics.command.VinCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DevicePropertiesHandler extends ReplyObserver<Reply<?>> {

	@Getter
	private final DeviceProperties deviceProperties = new DeviceProperties();

	@Override
	public void onNext(Reply<?> reply) {
		final DeviceProperty deviceProperty = (DeviceProperty) reply.getCommand();
		log.info("Received Device Property: {}", reply);

		if (deviceProperty instanceof Codec<?>) {
			final Object decode = ((Codec<?>) deviceProperty).decode(null, reply.getRaw());
			if (decode == null) {
				deviceProperties.update(deviceProperty.getLabel(), reply.getRaw());
			} else {
				deviceProperties.update(deviceProperty.getLabel(), decode.toString());
			}
		} else {
			deviceProperties.update(deviceProperty.getLabel(), reply.getRaw());
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
