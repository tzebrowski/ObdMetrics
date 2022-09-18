package org.obd.metrics.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"code"})
public class DiagnosticTroubleCode {
	private final String code;
	private final String category;
}