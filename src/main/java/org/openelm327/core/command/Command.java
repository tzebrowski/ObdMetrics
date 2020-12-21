package org.openelm327.core.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

//https://www.sparkfun.com/datasheets/Widgets/ELM327_AT_Commands.pdf
@ToString
@AllArgsConstructor
public abstract class Command {
	@Getter
	final String uid = UUID.randomUUID().toString();

	@Getter
	final String value;

	@Getter
	final String label;
}
