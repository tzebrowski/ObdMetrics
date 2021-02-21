package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VinCommand extends DeviceProperty implements Codec<String> {

	public VinCommand() {
		super("09 02", "VIN");
	}

	@Override
	public String decode(PidDefinition pid, String raw) {
		log.debug("Decoding the message: {}", raw);
		
		int indexOf = raw.indexOf("49");
		
		if (indexOf <= 0) {
			log.warn("Failed to decode VIN. Answer code != 49. Message:{}",raw);
			return null;
		}

		final String hex = raw.substring(indexOf + 2 + 4, raw.length()).replaceAll("[a-zA-Z0-9]{1}\\:", "");
		if (hex.length() % 2 != 0) {
			log.warn("Failed to decode VIN. Incorrect hex");
			return null;
		}

		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < hex.length(); i = i + 2) {
			String s = hex.substring(i, i + 2);
			int n = Integer.valueOf(s, 16);
			buffer.append((char) n);
		}
		if (buffer.length() != 17) {
			log.warn("Failed to decode VIN. Length is different than 17. Output: {}", buffer);
			return null;
		}
		return buffer.toString();
	}
}
