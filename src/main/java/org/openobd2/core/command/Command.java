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
	final String uid = UUID.randomUUID().toString();

	final String query;

	@Getter
	final String label;
	
	public byte[] getQuery() {
		return (query + "\r").getBytes();
	}
}
