package org.obd.metrics.raw;

import org.obd.metrics.codec.batch.BatchMessagePatternEntry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "bytes")
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class BatchMessage implements RawMessage {

	@Getter
	private final BatchMessagePatternEntry pattern;

	@Getter
	private final byte[] bytes;
}
