package org.openobd2.core.converter;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.openobd2.core.command.Command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements ConverterRegistry {

	private final Map<Command, Converter<?>> registry = new HashedMap<Command, Converter<?>>();
	private final FormulaEvaluator formulaEvaluator;

	@Override
	public void register(Command command, Converter<?> converter) {
		registry.put(command, converter);
	}

	@Override
	public Optional<Converter<?>> findConverter(Command command) {
		Converter<?> converter = registry.get(command);
		if (null == converter) {
			// no dedicated converter
			converter = formulaEvaluator;
		}
		return Optional.ofNullable(converter);
	}
}
