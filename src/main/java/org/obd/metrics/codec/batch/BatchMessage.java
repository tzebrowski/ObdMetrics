package org.obd.metrics.codec.batch;

import org.obd.metrics.model.RawMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "bytes")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BatchMessage implements RawMessage {

	@Getter
	private final BatchMessagePatternEntry pattern;

	@Getter
	private final byte[] bytes;

	@Override
	public String getMessage() {
		return new String(bytes);
	}

	public static BatchMessage instance(byte[] message) {
		return new BatchMessage(null, message);
	}
}
