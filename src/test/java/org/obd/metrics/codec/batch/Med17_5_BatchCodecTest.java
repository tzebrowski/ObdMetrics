package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Med17_5_BatchCodecTest {
	
	@Test
	public void stn_test() {
		
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.codec.batch.AbstractBatchCodec");
		logger.setLevel(Level.TRACE);
		
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("15")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("11")));

		
		final Adjustments optional = Adjustments
		        .builder()
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .responseLengthEnabled(Boolean.FALSE)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(1)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .cacheConfig(CachePolicy.builder().resultCacheEnabled(false).build())
		        .batchEnabled(true)
		        .build();
		
		final byte[] message = "00D0:41155AFF0BFF1:0C000004001100".getBytes();
		final BatchCodec codec = BatchCodec.builder().adjustments(optional).commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponse.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("15")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		
	}
	
	
	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("06")));
		final byte[] message = "00F0:41010007E1001:030000040005002:0680AAAAAAAAAA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponse.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), batchMessage);

	}

	@Test
	public void case_02() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("0F")));
		commands.add(new ObdCommand(registry.findBy("11")));
		final byte[] message = "00C0:4105000BFF0C1:00000F001100AA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponse.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);
	}

	@Test
	public void case_03() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		final byte[] message = "4105000C0000".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponse.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
	}

	@Test
	public void case_04() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("06")));
		commands.add(new ObdCommand(registry.findBy("07")));

		final byte[] message = "0110:41010007E1001:030000040005002:0680078BAAAAAA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponse.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("07")), batchMessage);
	}
}
