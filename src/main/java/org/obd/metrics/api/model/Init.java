package org.obd.metrics.api.model;

import java.util.List;

import org.obd.metrics.command.group.CommandGroup;
import org.obd.metrics.command.group.DefaultCommandGroup;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

@Builder
public class Init {
	
	@Builder
	@ToString
	public static class Header {
		@Getter
		@Default
		private String header = "";

		@Getter
		@Default
		private String mode = "";		
	}
	
	public static final Init DEFAULT = Init.builder()
	        .delayAfterInit(0)
	        .protocol(Protocol.AUTO)
	        .sequence(DefaultCommandGroup.INIT)
	        .build();

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
	private long delayAfterReset = 0l;
	
	@Getter
	@Default
	private long delayAfterInit = 0l;

	@Getter
	@NonNull
	@Default
	private CommandGroup<?> sequence = DefaultCommandGroup.INIT;
	
	@Getter
	@Default
	private Protocol protocol = Protocol.AUTO;

	@Getter
	@Singular
	private List<Header> headers;
}