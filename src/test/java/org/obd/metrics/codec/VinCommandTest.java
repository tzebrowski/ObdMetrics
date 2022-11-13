package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class VinCommandTest {
	
	
	@ParameterizedTest
	@CsvSource(value = { 
		"0140:4902015756571:5A5A5A314B5A412:4D363930333932;WVWZZZ1KZAM690392",//correct
		"0140:4802015756571:5A5A5A314B5A412:4D363930333932;", // incorrect success code
		"0140:4902015756571:5A5A5A314B5A412:4D363930333;", // incorrect hex
	}, delimiter = ';')
	public void hexCommandTest(String raw,String expected) {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(11000l));
		String decode = metadataDecoder.decode(ConnectorResponseFactory.wrap(raw.getBytes()));
		Assertions.assertThat(decode).isEqualTo(expected);
	}
}
