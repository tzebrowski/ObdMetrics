package org.openobd2.core.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//https://www.sparkfun.com/datasheets/Widgets/ELM327_AT_Commands.pdf
@AllArgsConstructor
@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	protected final String uid = UUID.randomUUID().toString();

	protected final String query;

	@Getter
	protected final String label;

	public Long getDelayBeforeExecution() {
		return null;
	}

	public byte[] getQuery() {
		return (query + "\r").getBytes();
	}

	@Override
	public String toString() {
		return "[query=" + query + "]";
	}
}
