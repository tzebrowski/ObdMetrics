package org.openobd2.core.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

//https://www.sparkfun.com/datasheets/Widgets/ELM327_AT_Commands.pdf
@ToString(of = { "query", "type" })
@AllArgsConstructor
@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	final String uid = UUID.randomUUID().toString();

	@Getter
	final String query;

	@Getter
	final String label;

	@Getter
	final String type = this.getClass().getSimpleName();

}
