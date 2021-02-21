package org.obd.metrics;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class DeviceProperties {

	@Getter
	private final Map<String, String> properties = new HashMap<>();

	void add(String key, String value) {
		properties.put(key, value);
	}
}
