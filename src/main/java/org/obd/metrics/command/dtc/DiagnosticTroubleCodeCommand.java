package org.obd.metrics.command.dtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeCommand extends Command implements Codec<List<DiagnosticTroubleCode>> {

	private static final String pattern = "[a-zA-Z0-9]{1}\\:";
	private static final int codeLength = 6;

	protected final PidDefinition pid;

	public DiagnosticTroubleCodeCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public List<DiagnosticTroubleCode> decode(final PidDefinition pid, final ConnectorMessage raw) {

		if (raw.isEmpty()) {
			return Collections.emptyList();
		} else {
			final Optional<List<DiagnosticTroubleCode>> decode = decode(raw.getMessage());
			if (decode.isPresent()) {
				final List<DiagnosticTroubleCode> codes = decode.get();
				if (log.isDebugEnabled()) {
					codes.forEach(dtc -> log.debug("Found DTC: {}", dtc));
				}
				return codes;
			}
		}
		return Collections.emptyList();
	}

	private Optional<List<DiagnosticTroubleCode>> decode(final String rx) {
		final String successCode = pid.getSuccessCode();
		final int successCodeIndex = rx.indexOf(successCode);
		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>();

		if (successCodeIndex >= 0) {
			final String codes = rx.substring(successCodeIndex + successCode.length())
					.replaceAll(pattern, "")
					.replaceAll("48", "");

			for (int i = 0; i < codes.length() / codeLength; i++) {
				final int beginIndex = i * codeLength;
				dtcList.add(DiagnosticTroubleCode.builder().code(codes.substring(beginIndex, beginIndex + codeLength)).build());
			}
			return Optional.of(dtcList);
		} else {
			return Optional.empty();
		}
	}
}
