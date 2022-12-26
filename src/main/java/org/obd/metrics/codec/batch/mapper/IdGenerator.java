package org.obd.metrics.codec.batch.mapper;

import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	private static final int _10 = 10;
	private static final int _100000 = 10000;

	static long generate(final int length, final long pidId, int index,final ConnectorResponse connectorResponse) {
		int postfix = 0;
		long prefix = pidId * _100000;

		if (length >= 1 && connectorResponse.remaining() >= index + 1) {
			int digit = connectorResponse.byteAt(index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;
		}

		if (length >= 2 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 10;
		}

		if (length >= 3 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 100;

		}

		if (length >= 4 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.byteAt(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 100;
		}

		final long id = prefix + postfix;
		if (log.isTraceEnabled()) {
			log.trace("{} = {}", pidId, id);
		}
		return id;
	}
}
