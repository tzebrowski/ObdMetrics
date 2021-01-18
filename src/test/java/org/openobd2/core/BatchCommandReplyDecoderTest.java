package org.openobd2.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.BatchCommandReplyDecoder;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidRegistry;

public class BatchCommandReplyDecoderTest {
	
	@Test
	public void t0() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			String message = "00b0:410c000010001:000b660d000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B66");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"41100010");
		}
	}
	
	@Test
	public void t1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("0F")));
									
			String message = "00f0:410c000010001:000b660d0005222:0f370000000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B66");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")),"410F37");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),"410522");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"41100010");
		}
	}
	
	
	@Test
	public void t2() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("0F")));

			String message = "410c0000100000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"41100000");
		}
	}

	
	@Test
	public void t3() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("0F")));

			String message = "0090:410c000010001:000b6600000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"41100010");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B66");
		}
	}
	
	
	@Test
	public void t4() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("0F")));

			String message = "00d0:410c000010001:000b660d000522";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B66");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),"410522");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"41100010");
		}
	}
	
	@Test
	public void t5() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("10")));
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("0F")));
			pids.add(new ObdCommand(registry.findBy("11")));
			

			String message = "00f0:410c000010001:000b660d0005222:11260000000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B66");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")),"411126");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),"410522");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
		}
	}

	
	@Test
	public void t6() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("0B")));
			pids.add(new ObdCommand(registry.findBy("0C")));
			pids.add(new ObdCommand(registry.findBy("0D")));
			pids.add(new ObdCommand(registry.findBy("0E")));
			pids.add(new ObdCommand(registry.findBy("0F")));
			pids.add(new ObdCommand(registry.findBy("10")));
			
			      
			String message = "00f0:410b650c00001:0d000e800f2f102:00000000000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0E")),"410E80");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),"410C0000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),"410B65");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")),"410D00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")),"4110d000");
		}
	}
	
	
	@Test
	public void t7() throws IOException {
		//01 03 04 05 06 07
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {
			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> pids = new ArrayList<>();
			pids.add(new ObdCommand(registry.findBy("01")));
			pids.add(new ObdCommand(registry.findBy("03")));
			pids.add(new ObdCommand(registry.findBy("04")));
			pids.add(new ObdCommand(registry.findBy("05")));
			pids.add(new ObdCommand(registry.findBy("06")));
			pids.add(new ObdCommand(registry.findBy("07")));
			
			      
			String message = "0110:4101000771611:0300000400051c2:06800781000000";
			BatchCommandReplyDecoder decoder = new BatchCommandReplyDecoder();
			Map<ObdCommand, String> values = decoder.decode(pids, message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),"41051c");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")),"410400");
		}
	}
}
