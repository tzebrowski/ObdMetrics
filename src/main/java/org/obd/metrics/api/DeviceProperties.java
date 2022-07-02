package org.obd.metrics.api;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class DeviceProperties {

	@Getter
	private final Map<String, String> properties;

	@Getter
	private final Set<String> capabilities;

}
