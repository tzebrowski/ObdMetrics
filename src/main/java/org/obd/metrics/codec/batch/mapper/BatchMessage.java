package org.obd.metrics.codec.batch.mapper;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.DecimalReceiver;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(of = "mapping")
@EqualsAndHashCode(of = "message")
final class BatchMessage implements ConnectorResponse {

	private final BatchCommandMapping mapping;

	private final ConnectorResponse delegate;

	private long id = -1L;

	private boolean cacheable;

	BatchMessage(final BatchCommandMapping mapping, final ConnectorResponse delegate) {
		this.mapping = mapping;
		this.delegate = delegate;

		if (mapping == null) {
			this.cacheable = false;
		} else {
			this.cacheable = mapping.getCommand().getPid().getCacheable();
			if (this.cacheable) {
				this.id = IdGenerator.generate(mapping.getCommand().getPid().getLength(),
						mapping.getCommand().getPid().getId(), mapping.getStart(), delegate);
			}
		}
	}
	
	@Override
	public long capacity() {
		return delegate.capacity();
	}
	
	@Override
	public String getMessage() {
		return delegate.getMessage();
	}

	@Override
	public int remaining() {
		return delegate.remaining();
	}

	@Override
	public void exctractDecimals(final PidDefinition pidDefinition, final DecimalReceiver decimalReceiver) {
		for (int pos = mapping.getStart(), j = 0; pos < mapping.getEnd(); pos += 2, j++) {
			decimalReceiver.receive(j, toDecimal(pos));
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

	@Override
	public byte byteAt(int index) {
		return delegate.byteAt(index);
	}
}
