package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VinCommand extends DeviceProperty implements Codec<String> {
	final static String mode = "09";

	public VinCommand() {
		super(mode + " 02", "VIN");
	}

	@Override
	public String decode(PidDefinition pid, String raw) {
		log.debug("Decoding the message: {}", raw);
		final int indexOf = raw.indexOf(new MetricsDecoder().getPredictedAnswerCode(mode));

		if (indexOf <= 0) {
			log.warn("Failed to decode VIN. Answer code != 49. Message:{}", raw);
			return null;
		}

		final String hex = raw.substring(indexOf + 2 + 4).replaceAll("[a-zA-Z0-9]{1}\\:", "");
		if (hex.length() % 2 != 0) {
			log.warn("Failed to decode VIN. Incorrect hex");
			return null;
		}

		final String decoded = String.valueOf(Hex.decode(hex.toCharArray()));

		if (decoded.length() != 17) {
			log.warn("Failed to decode VIN. Length is different than 17. Output: {}", decoded);
			return null;
		}

		return decoded;
	}

}
