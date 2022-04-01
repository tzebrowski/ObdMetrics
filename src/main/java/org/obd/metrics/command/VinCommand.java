package org.obd.metrics.command;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.connection.Characters;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VinCommand extends DeviceProperty implements Codec<String> {
	private final static String mode = "09";
	private final String predictedAnswerCode = new AnswerCodeCodec(false).getPredictedAnswerCode(mode);

	public VinCommand() {
		super(mode + " 02", "VIN");
	}

	@Override
	public String decode(PidDefinition pid, RawMessage raw) {
		log.debug("Decoding the message: {}", raw);
		final String message = Characters.normalize(raw.getMessage());
		final int indexOf = message.indexOf(predictedAnswerCode);

		if (indexOf <= 0) {
			log.warn("Failed to decode VIN. Answer code != {}. Message:{}", predictedAnswerCode, message);
			return null;
		}

		final String vin = Hex.decode(message.substring(indexOf + 2 + 4).replaceAll("[a-zA-Z0-9]{1}\\:", ""));
		log.debug("Decoded VIN: {}", vin);
		return vin;
	}
}
