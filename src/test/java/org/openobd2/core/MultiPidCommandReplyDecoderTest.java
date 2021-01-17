package org.openobd2.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.MultiPidCommandReplyDecoder;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

public class MultiPidCommandReplyDecoderTest {

	
	@Test
	public void t0() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));

			String message = "00b0:410c000010001:000b660d000000";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0B","66");
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("0D","00");
			Assertions.assertThat(values).containsEntry("10","0010");
		}
	}
	
	@Test
	public void t1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));
			pids.add(registry.findBy("0F"));

			String message = "00f0:410c000010001:000b660d0005222:0f370000000000";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0B","66");
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("0F","37");
			Assertions.assertThat(values).containsEntry("05","22");
			Assertions.assertThat(values).containsEntry("0D","00");
			Assertions.assertThat(values).containsEntry("10","0010");
		}
	}
	
	
	@Test
	public void t2() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));
			pids.add(registry.findBy("0F"));

			String message = "410c0000100000";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("10","0000");
		}
	}

	
	@Test
	public void t3() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));
			pids.add(registry.findBy("0F"));

			String message = "0090:410c000010001:000b6600000000";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("10","0010");
			Assertions.assertThat(values).containsEntry("0B","66");
		}
	}
	
	
	@Test
	public void t4() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));
			pids.add(registry.findBy("0F"));

			String message = "00d0:410c000010001:000b660d000522";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0B","66");
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("05","22");
			Assertions.assertThat(values).containsEntry("0D","00");
			Assertions.assertThat(values).containsEntry("10","0010");
		}
	}
	
	@Test
	public void t5() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<PidDefinition> pids = new ArrayList<>();
			pids.add(registry.findBy("0C"));
			pids.add(registry.findBy("10"));
			pids.add(registry.findBy("0B"));
			pids.add(registry.findBy("0D"));
			pids.add(registry.findBy("05"));
			pids.add(registry.findBy("0F"));
			pids.add(registry.findBy("11"));
			

			String message = "00f0:410c000010001:000b660d0005222:11260000000000";
			MultiPidCommandReplyDecoder decoder = new MultiPidCommandReplyDecoder();
			Map<String, String> values = decoder.decode("01",pids, message);
			
			Assertions.assertThat(values).containsEntry("0B","66");
			Assertions.assertThat(values).containsEntry("0C","0000");
			Assertions.assertThat(values).containsEntry("11","26");
			Assertions.assertThat(values).containsEntry("05","22");
			Assertions.assertThat(values).containsEntry("0D","00");
		}
	}
	
}
