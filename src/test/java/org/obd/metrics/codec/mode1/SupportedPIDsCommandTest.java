package org.obd.metrics.codec.mode1;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class SupportedPIDsCommandTest {

	@Test
	public void group_invalid() {

		final String rawData = "2100BE3E2F00";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21000l);
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));
		Assertions.assertThat(result).isNotNull().isEmpty();
	}

	@Test
	public void goup00_19tdi() {

		final String rawData = "4100983F8010";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21000l);
		
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);

		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "04", "05", "0b",
		        "0c", "0d", "0e", "0f", "10", "11", "1c");

	}

	
	@Test
	public void goup00() {

		final String rawData = "4100BE3E2F00";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21000l);
		
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);

		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
		        "07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");
	}

	@Test
	public void group20() {
		final String rawData = "4120a0001000";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21001l);
		
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));
		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "14");
	}

	@Test
	public void group20_2() {

		final String rawData = "4120A005B011";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21001l);
		
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));
		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "0e", "10", "11", "13", "14",
		        "1c");
	}

	@Test
	public void group40() {
		final String rawData = "4140FED00400";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21002l);
		
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));
		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "02", "03", "04", "05", "06", "07",
		        "09", "0a", "0c", "16");
	}
}
