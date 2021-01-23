package org.obd.metrics.pid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "pid" })
public class PidDefinition {

	@NonNull
	private int length;

	@NonNull
	private String formula;

	@NonNull
	private String mode;

	@NonNull
	private String pid;

	@NonNull
	private String units;

	@NonNull
	private String description;

	@NonNull
	private String min;

	@NonNull
	private String max;

	private int order = 1000;

}
