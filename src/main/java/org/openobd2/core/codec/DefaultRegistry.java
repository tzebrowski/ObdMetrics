package org.openobd2.core.codec;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;

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
		Codec<?> converter = registry.get(command);
		if (null == converter) {
			// no dedicated converter
			converter = formulaEvaluator;
		}
		return Optional.ofNullable(converter);
	}
}
