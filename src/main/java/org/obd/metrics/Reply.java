package org.obd.metrics;

import java.util.Optional;

import org.obd.metrics.command.Command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	@Getter
	protected final T command;

	@Getter
	protected final String raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();

	@SuppressWarnings("unchecked")
	public <V> Optional<V> isCommandInstanceOf(Class<V> clazz) {
		if (clazz.isAssignableFrom(command.getClass())) {
			return Optional.of((V) command);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append("Reply [com=");
		builder.append(command);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
