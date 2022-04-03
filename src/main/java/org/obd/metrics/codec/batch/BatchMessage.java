package org.obd.metrics.codec.batch;

import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.DecimalReceiver;
import org.obd.metrics.raw.RawMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "bytes")
final class BatchMessage implements RawMessage {

	private final BatchMessageVariablePatternItem pattern;

	@Getter
	private final byte[] bytes;

	private final Long id;

	private boolean cachable;

	BatchMessage(BatchMessageVariablePatternItem pattern, byte[] bytes) {
		this.pattern = pattern;
		this.bytes = bytes;
		if (bytes == null || pattern == null) {
			this.cachable = false;
			this.id = -1L;
		} else {
			this.cachable = pattern.getCommand().getPid().getCacheable();
			if (this.cachable) {
				this.id = IdGenerator.generate(pattern.getCommand().getPid().getLength(),
				        pattern.getCommand().getPid().getId(), pattern.getStart(), bytes);
			} else {
				this.id = -1L;
			}
		}
	}

	@Override
	public void exctractDecimals(PidDefinition pidDefinition, DecimalReceiver decimalHandler) {
		for (int pos = pattern.getStart(),
		        j = 0; pos < pattern.getEnd(); pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalHandler.receive(j, decimal);
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
