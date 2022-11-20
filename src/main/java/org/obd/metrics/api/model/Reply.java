package org.obd.metrics.api.model;

import org.obd.metrics.command.Command;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	@Setter
	@Getter
	protected T command;

	@Setter
	@Getter
	protected ConnectorResponse raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(100);
		builder.append("Reply [com=");
		builder.append(command);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
