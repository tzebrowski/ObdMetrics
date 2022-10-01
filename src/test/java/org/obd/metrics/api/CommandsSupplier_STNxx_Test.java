package org.obd.metrics.api;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

// [22, 7002, 13, 14, 15, 7003, 7006, 6, 7005, 18, 7018, 7007, 7015, 7014, 7017, 7016, 7019, 7020]
// [[priority=0, query=STPX H:18DA10F1, D:22 181F 1937 130A 1924, R:3], [priority=2, query=STPX H:18DA10F1, D:22 1935 1302 3A58 18BA 1004, R:3], [priority=3, query=STPX H:18DA10F1, D:22 19BD, R:1], [priority=4, query=STPX H:18DA10F1, D:22 3A41, R:1], [priority=6, query=STPX H:18DA10F1, D:22 3813, R:1], [priority=0, query=STPX H:18DB33F1, D:01 15 0C 0D 11, R:2], [priority=1, query=STPX H:18DB33F1, D:01 0E, R:1], [priority=2, query=STPX H:18DB33F1, D:01 05, R:1]]


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
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.FALSE).build())
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
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
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
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
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
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
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
	
	@Test
	public void promoteSlowGoupTest() {
		final PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l)
		        .pid(13l)
		        .pid(18l)
		        
				.pid(pidRegistry.findBy(7019l).getId())
				.pid(pidRegistry.findBy(7016l).getId())
				.pid(pidRegistry.findBy(7002l).getId())
		        .pid(pidRegistry.findBy(7003l).getId())
		        .pid(pidRegistry.findBy(7005l).getId())
		        .pid(pidRegistry.findBy(7007l).getId())
			    .pid(pidRegistry.findBy(7006l).getId())
		        .pid(pidRegistry.findBy(7018l).getId())
		        .pid(pidRegistry.findBy(7015l).getId())
		        .pid(pidRegistry.findBy(7014l).getId())
		        .pid(pidRegistry.findBy(7017l).getId())
		        .pid(pidRegistry.findBy(7020l).getId())
		   
		        .build();
		
		Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
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
		
		Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		List<ObdCommand> collection = commandsSupplier.get();
		Assertions.assertThat(collection).isNotEmpty().hasSize(5);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1937 1924 181F 130A 1004 18BA 1935 1302 3A58, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 19BD, R:1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 3A41, R:1");

	
		extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
					.enabled(Boolean.TRUE)
					.promoteSlowGroupsEnabled(Boolean.FALSE).build())
				.batchEnabled(true)
				.responseLengthEnabled(true)
				.build();
		
		commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		collection = commandsSupplier.get();
		Assertions.assertThat(collection).isNotEmpty().hasSize(6);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1937 1924 181F 130A, R:3");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1004 18BA 1935 1302 3A58, R:3");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 19BD, R:1");
	}
}