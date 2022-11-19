package org.obd.metrics.codec.batch.mapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.DecimalReceiver;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class BatchMessage implements ConnectorResponse {

	private final BatchCommandMapping mapping;

	@Getter
	private final byte[] bytes;

	private long id = -1L;

	private boolean cacheable;
	private final int length;
	private String message;

	BatchMessage(final BatchCommandMapping mapping, final ConnectorResponse original) {
		this.mapping = mapping;
		this.bytes = original.getBytes();
		this.length = original.getLength();

		if (bytes == null || mapping == null) {
			this.cacheable = false;
		} else {
			this.cacheable = mapping.getCommand().getPid().getCacheable();
			if (this.cacheable) {
				this.id = IdGenerator.generate(mapping.getCommand().getPid().getLength(),
						mapping.getCommand().getPid().getId(), mapping.getStart(), bytes);
			}
		}
	}

	@Override
	public String getMessage() {
		if (message == null && bytes != null) {
			message = new String(Arrays.copyOf(bytes, length), StandardCharsets.ISO_8859_1);
		}
		return message;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public void exctractDecimals(final PidDefinition pidDefinition, final DecimalReceiver decimalReceiver) {
		for (int pos = mapping.getStart(), j = 0; pos < mapping.getEnd(); pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalReceiver.receive(j, decimal);
		}
	}

	@Override
	public boolean isCacheable() {
		return cacheable;
	}

	@Override
	public long id() {
		return id;
	}
}
