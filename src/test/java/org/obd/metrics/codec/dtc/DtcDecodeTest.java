package org.obd.metrics.codec.dtc;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.raw.RawMessage;

public class DtcDecodeTest {

	@Test
	public void case_1() {
		// P26E4-00
		// P2BC1-00
		// U1008-00
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final List<String> list = new DtcDecoder().decode(null,RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains("26E4-00").contains("2BC1-00");
	}
	
	@Test
	public void case_2() {
		// C405810
		final String rx = "5902CFC4058108";
		final List<String> list = new DtcDecoder().decode(null,RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains("C405-81");
	}
}
