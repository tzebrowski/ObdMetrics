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

	private Long id = -1L;

	private boolean cacheable;

	BatchMessage(final BatchMessageVariablePatternItem pattern, final byte[] bytes) {
		this.pattern = pattern;
		this.bytes = bytes;
		if (bytes == null || pattern == null) {
			this.cacheable = false;
		} else {
			this.cacheable = pattern.getCommand().getPid().getCacheable();
			if (this.cacheable) {
				this.id = IdGenerator.generate(pattern.getCommand().getPid().getLength(),
						pattern.getCommand().getPid().getId(), pattern.getStart(), bytes);
			}
		}
	}
	
	@Override
	public int getLength() {
		return bytes.length;
	}

	@Override
	public void exctractDecimals(final PidDefinition pidDefinition, final DecimalReceiver decimalReceiver) {
		for (int pos = pattern.getStart(), j = 0; pos < pattern.getEnd(); pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalReceiver.receive(j, decimal);
		}
	}

	@Override
	public boolean isCacheable() {
		return cacheable;
	}

	@Override
	public Long id() {
		return id;
	}
}
