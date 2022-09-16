package org.obd.metrics.api.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Dtc {

	@Getter
	private final Set<String> codes;
}
