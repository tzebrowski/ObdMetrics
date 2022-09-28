package org.obd.metrics.api;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class CommandsSupplier_STNxx_Test {
	
	@Test
	public void limitMode1QueryTest() {
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
		        .pid(24l) 
		        .pid(25l) 
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stnExtensionsEnabled(true)
				.batchEnabled(true)
				.responseLengthEnabled(true).build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra ,query,Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(4);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX D:01 15 0B 0C 11 0D 16, R:3");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX D:01 17, R:1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX D:01 0E, R:1");
		Assertions.assertThat(collection.get(3).getQuery()).isEqualTo("STPX D:01 0F, R:1");
	}

	@Test
	public void limitMode22QueryTest() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .pid(7021l)
		        .pid(7022l)
		        .pid(7023l)
		        .pid(7024l)
		        
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stnExtensionsEnabled(true)
				.batchEnabled(true)
				.responseLengthEnabled(true).build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delay(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra ,query, init);
		final List<ObdCommand> collection = commandsSupplier.get();
		

		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1802, R:1");
	}
	
	@Test
	public void noHeadersIncludedQueryTest() {
		final PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position
		        
				.pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stnExtensionsEnabled(Boolean.TRUE)
				.batchEnabled(true)
				.responseLengthEnabled(true)
				.build();
		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX D:22 130A 195A 1937 181F 1924 1000 182F, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX D:01 0B 0C 11, R:2");
	}
	

	@Test
	public void headersIncludedQueryTest() {
		final PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position
		        
		        .pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stnExtensionsEnabled(Boolean.TRUE)
				.batchEnabled(true)
				.responseLengthEnabled(true)
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delay(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DB33F1, D:01 0B 0C 11, R:2");
	}
}
