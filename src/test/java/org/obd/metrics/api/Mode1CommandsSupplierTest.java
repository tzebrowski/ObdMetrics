package org.obd.metrics.api;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.CodecTest.PidRegistryCache;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class Mode1CommandsSupplierTest {

	
	@Test
	public void containsOnlyUniquePid() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(22l) // O2 Voltage
		        .pid(23l) // AFR
				.pid(12l) // Boost
		        .pid(99l) // Intake pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .pid(9000l) // Battery voltage
		        .build();
		
		CommandsSuplier commandsSupplier = new Mode1CommandsSupplier(pidRegistry, true,query);
		List<ObdCommand> collection = commandsSupplier.map(query);
		Assertions.assertThat(collection).isNotEmpty().hasSize(3);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 15 0B 0C 11 0D");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("01 0E");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("01 0F");
	}
}
