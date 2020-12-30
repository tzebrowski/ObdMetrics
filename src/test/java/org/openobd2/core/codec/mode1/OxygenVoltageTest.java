package org.openobd2.core.codec.mode1;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidRegistry;

public class OxygenVoltageTest {

	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {
			
			
			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("01", "15"))).get();

			String rawData = "41155aff";
			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(44.6484375);
		}
	}
}
