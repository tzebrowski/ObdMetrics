package org.obd.metrics.api.model;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class VehicleCapabilities {

	@Getter
	private final Map<String, String> metadata;

	@Getter
	private final Set<String> capabilities;
	
	@Getter
	private final Set<DiagnosticTroubleCode> dtc;
}
