package org.obd.metrics.codec.giulia_2_0_gme;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.command.meta.NotEncodedCommand;
import org.obd.metrics.command.meta.TimeCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class MetadataDecoderTest {
	@Test
	public void vin_1_Test() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("mode01.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(11000l));
		
		String answer = "0140:4902015756571:5A5A5A314B5A412:4D363930333932";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("WVWZZZ1KZAM690392");
	}
	
	@Test
	public void vin_2_Test() {
		
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17001l));
		
		String answer = "0140:62F1905A41521:454145424E394B2:37363137323839";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("ZAREAEBN9K7617289");
	}
	
	@Test
	public void ecuSerialNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17002l));
		
		String answer = "0120:62F18C5444341:313930393539452:3031343430";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("TD4190959E01440");
	}
	
	@Test
	public void ecuSofwareNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17003l));
		
		String answer = "00E0:62F1945031341:315641304520202:20";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("P141VA0E");
	}
	
	
	@Test
	public void fiatDrawingNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17004l));
		
		String answer = "00E0:62F1913532301:353533323020202:20";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("52055320");
	}
	
	@Test
	public void ecuTypeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17005l));
		
		String answer = "00E0:62F1924D4D311:304A41485732332:32";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("MM10JAHW232");
	}
	
	@Test
	public void sparePartNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17006l));
		
		String answer = "00E0:62F1873530351:353938353220202:20";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("50559852");
	}
	
	
	@Test
	public void homologationNumberTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		HexCommand metadataDecoder = new HexCommand(pidDefinitionRegistry.findBy(17007l));
		
		String answer = "0090:62F1964548411:423030";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("EHAB00");
	}
		
	@Test
	public void softwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(17008l));
		
		String answer = "62F1950000";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0000");
	}

	@Test
	public void hardwareVersion() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(17009l));
		
		String answer = "62F19300";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("00");
	}
	
	@Test
	public void ecuIsoCodeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		NotEncodedCommand metadataDecoder = new NotEncodedCommand(pidDefinitionRegistry.findBy(17010l));
		
		String answer = "0080:62F1A50001501:7517";
		String decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo("0001507517");
	}
	
	
	@Test
	public void operatingTimeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		TimeCommand metadataDecoder = new TimeCommand(pidDefinitionRegistry.findBy(17012l));
		
		String answer = "6210080000BFC8";
		Integer decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo(49096);
	}
	
	@Test
	public void functioningTimeTest() {
		PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		TimeCommand metadataDecoder = new TimeCommand(pidDefinitionRegistry.findBy(17011l));
		
		String answer = "6220080000BFC7";
		Integer decode = metadataDecoder.decode(null, ConnectorResponseFactory.wrap(answer.getBytes()));
		Assertions.assertThat(decode).isNotNull().isEqualTo(49095);
	}
}
