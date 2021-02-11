package org.obd.metrics.pid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "pid" })
public class PidDefinition {

	@Getter
	@NonNull
	private Long id;

	public static enum Type {
		INT, DOUBLE, SHORT
	}

	@Getter
	@NonNull
	private int length;

	@Getter
	@NonNull
	private String formula;

	@Getter
	@NonNull
	private String mode;

	@Getter
	@NonNull
	private String pid;

	@Getter
	@NonNull
	private String units;

	@Getter
	@NonNull
	private String description;

	@Getter
	@NonNull
	private String min;

	@Getter
	@NonNull
	private String max;

	// optional
	private Type type;

	public Type getType() {
		return type == null ? Type.DOUBLE : type;
	}
}
