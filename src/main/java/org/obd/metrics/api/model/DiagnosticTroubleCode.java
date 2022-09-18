package org.obd.metrics.api.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@EqualsAndHashCode(of = { "code" })
public class DiagnosticTroubleCode {
	public static enum Category {
		Body, Chassis, Powertrain, Network
	}

	private final String code;
	private final Category category;
	private final String description;	
}