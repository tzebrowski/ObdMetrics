package org.obd.metrics.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	protected final String uid = UUID.randomUUID().toString();

	@Getter
	protected final String query;

	@Getter
	protected final String label;
	
	@Override
	public String toString() {
		return "[query=" + query + "]";
	}
}
