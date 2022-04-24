package org.obd.metrics.api;

import org.obd.metrics.command.group.CommandGroup;
import org.obd.metrics.command.group.DefaultCommandGroup;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class InitConfiguration {

	public static final InitConfiguration DEFAULT = InitConfiguration.builder()
	        .delay(0)
	        .protocol(Protocol.AUTO)
	        .sequence(DefaultCommandGroup.INIT).build();

	public enum Protocol {
		AUTO(0), CAN_11(6), CAN_29(7);

		@Getter
		private int type;

		Protocol(int type) {
			this.type = type;
		}
	}

	@Getter
	@Default
	private long delay = 0l;

	@Getter
	@NonNull
	@Default
	private CommandGroup<?> sequence = DefaultCommandGroup.INIT;

	@Getter
	@Default
	private Protocol protocol = Protocol.AUTO;

	@Getter
	@Default
	private String header = "";
}