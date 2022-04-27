package org.obd.metrics.api;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.CodecTest.PidRegistryCache;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class NumberOfLinesTest {

	@Test
	public void threeLines() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(5l) // engine load
				.pid(7l)  // Short fuel trim
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, true,query);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);

		//ends with 3 - means three lines in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 04 06 3");
	}

	@Test
	public void twoLines() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(5l) // engine load
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, true,query);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 04 2");
	}
	
	@Test
	public void twoLines_2() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, true,query);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 2");
	}
	
	@Test
	public void twoLines_3() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, true,query);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);

		//ends with 2 - means two lines in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 2");
	}
	
	@Test
	public void oneLine() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, true,query);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		//ends with 1 - means one line in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 1");
	}
}
