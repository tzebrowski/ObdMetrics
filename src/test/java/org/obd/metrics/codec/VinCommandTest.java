package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.MetadataCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class VinCommandTest {
	
	@Test
	public void correctVin() {
		String raw = "0140:4902015756571:5A5A5A314B5A412:4D363930333932";
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		MetadataCommand metadataDecoder = new MetadataCommand(pidDefinitionRegistry.findBy(11000l));
		String decode = metadataDecoder.decode(null, RawMessage.wrap(raw.getBytes()));
		Assertions.assertThat(decode).isEqualTo("WVWZZZ1KZAM690392");
	}

	@Test
	public void noSuccessCode() {
		String raw = "0140:4802015756571:5A5A5A314B5A412:4D363930333932";

		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		MetadataCommand metadataDecoder = new MetadataCommand(pidDefinitionRegistry.findBy(11000l));
		String decode = metadataDecoder.decode(null, RawMessage.wrap(raw.getBytes()));
		Assertions.assertThat(decode).isEqualTo(null);
	}

	@Test
	public void incorrectHex() {
		String raw = "0140:4902015756571:5A5A5A314B5A412:4D363930333";

		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		MetadataCommand metadataDecoder = new MetadataCommand(pidDefinitionRegistry.findBy(11000l));
		String decode = metadataDecoder.decode(null, RawMessage.wrap(raw.getBytes()));
		Assertions.assertThat(decode).isEqualTo(null);
	}
}
