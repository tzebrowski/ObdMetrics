package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements CodecRegistry {

	private final Map<PidDefinition, Codec<?>> registry = new HashMap<>();
	private final Codec<Number> fallbackCodec;

	@Override
	public void register(final PidDefinition pid, final Codec<?> codec) {
		registry.put(pid, codec);
	}

	@Override
	public Codec<?> findCodec(final PidDefinition pid) {
		Codec<?> codec = registry.get(pid);

		if (null == codec) {
			// no dedicated codec
			codec = fallbackCodec;
		}
	
		return codec;
	}
}
