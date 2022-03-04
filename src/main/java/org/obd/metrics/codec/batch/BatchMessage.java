package org.obd.metrics.codec.batch;

import org.obd.metrics.connection.Characters;
import org.obd.metrics.model.RawMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "message")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BatchMessage implements RawMessage {

	@Getter
	private final String message;

	@Getter
	private final BatchMessagePatternEntry pattern;

	public static BatchMessage instance(String message) {
		return new BatchMessage(Characters.normalize(message), null);
	}
}
