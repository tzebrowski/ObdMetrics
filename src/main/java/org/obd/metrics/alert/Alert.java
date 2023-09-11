package org.obd.metrics.alert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Alert {
	final Number value;
	final long timestamp;
}