package org.obd.metrics.codec;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements CodecRegistry {

	private final Map<Command, Codec<?>> registry = new HashedMap<Command, Codec<?>>();
	private final FormulaEvaluator formulaEvaluator;

	@Override
	public void register(Command command, Codec<?> codec) {
		registry.put(command, codec);
	}

	@Override
	public Optional<Codec<?>> findCodec(Command command) {
		Codec<?> codec = registry.get(command);

		if (null == codec) {
			if (command instanceof Codec) {
				codec = (Codec<?>) command;
			}
			
			if (null == codec) {
				// no dedicated converter
				codec = formulaEvaluator;
			}
		}
		return Optional.ofNullable(codec);
	}
}
