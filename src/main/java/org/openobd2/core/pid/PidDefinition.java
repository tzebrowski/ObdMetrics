package org.openobd2.core.pid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor()
public class PidDefinition {

	private int length;

	private String formula;

	private String mode;
	private String pid;
	
	private String units;

	private String description;
	private String min;
	private String max;
}
