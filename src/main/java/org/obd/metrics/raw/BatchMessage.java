package org.obd.metrics.raw;

import org.obd.metrics.codec.batch.BatchMessagePatternEntry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "bytes")
public final class BatchMessage implements RawMessage {

	@Getter
	private final BatchMessagePatternEntry pattern;

	@Getter
	private final byte[] bytes;

	private final Long id;

	private boolean cachable;

	public BatchMessage(BatchMessagePatternEntry pattern, byte[] bytes) {
		this.pattern = pattern;
		this.bytes = bytes;
		if (this.bytes == null || pattern == null) {
			cachable = false;
			this.id = -1L;
		} else {
			cachable = true;
			this.id = IdGenerator.generateId(pattern.getCommand().getPid().getLength(),
			        pattern.getCommand().getPid().getId(), pattern.getStart(), bytes);
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
