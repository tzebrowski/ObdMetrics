package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.AnswerCodeCodec;
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

		final String predictedAnswerCode = new AnswerCodeCodec().getPredictedAnswerCode(mode);
		final int indexOf = raw.indexOf(predictedAnswerCode);

		if (indexOf <= 0) {
			log.warn("Failed to decode VIN. Answer code != {}. Message:{}", predictedAnswerCode, raw);
			return null;
		}

		final String vin = Hex.decode(raw.substring(indexOf + 2 + 4).replaceAll("[a-zA-Z0-9]{1}\\:", ""));
		log.debug("Decoded VIN: {}", vin);
		return vin;
	}
}
