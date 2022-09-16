package org.obd.metrics.codec.dtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtcDecoder implements Codec<List<String>> {
	private static final String pattern = "[a-zA-Z0-9]{1}\\:";
	private static final int codeLength = 6;
	private static final String successCode = "5902CF";

	@Override
	public List<String> decode(PidDefinition pid, RawMessage raw) {
		if (raw.isEmpty()) {
			return Collections.emptyList();
		} else {
			final Optional<List<String>> decode = decode(raw.getMessage());
			if (decode.isPresent()) {
				final List<String> codes = decode.get();
				if (log.isDebugEnabled()) {
					codes.forEach(dtc -> log.debug("Found DTC: {}", dtc));
				}
				return codes;
			}
		}
		return Collections.emptyList();
	}

	private Optional<List<String>> decode(final String rx) {

		final int successCodeIndex = rx.indexOf(successCode);
		final List<String> dtcList = new ArrayList<>();

		if (successCodeIndex >= 0) {
			final String codes = rx.substring(successCodeIndex + successCode.length()).replaceAll(pattern, "")
					.replaceAll("48", "");

			for (int i = 0; i < codes.length() / codeLength; i++) {
				final int beginIndex = i * codeLength;
				final String code = codes.substring(beginIndex, beginIndex + codeLength);
				dtcList.add(code.substring(0, 4) + "-" + code.substring(4, code.length()));
			}
			return Optional.of(dtcList);
		} else {
			return Optional.empty();
		}
	}
}
