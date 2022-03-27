package org.obd.metrics.codec.batch;

import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.DecimalHandler;
import org.obd.metrics.raw.RawMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "bytes")
final class BatchMessage implements RawMessage {

	private final BatchMessageVariablePatternEntry pattern;

	@Getter
	private final byte[] bytes;

	private final Long id;

	private boolean cachable;

	BatchMessage(BatchMessageVariablePatternEntry pattern, byte[] bytes) {
		this.pattern = pattern;
		this.bytes = bytes;
		if (bytes == null || pattern == null) {
			this.cachable = false;
			this.id = -1L;
		} else {
			this.cachable = true;
			this.id = IdGenerator.generate(pattern.getCommand().getPid().getLength(),
			        pattern.getCommand().getPid().getId(), pattern.getStart(), bytes);
		}
	}

	@Override
	public void toDecimals(PidDefinition pidDefinition, DecimalHandler decimalHandler) {
		for (int pos = pattern.getStart(),
		        j = 0; pos < pattern.getEnd(); pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalHandler.handle(j, decimal);
		}
	}

	@Override
	public boolean isCachable() {
		return cachable;
	}

	@Override
	public Long id() {
		return id;
	}
}
