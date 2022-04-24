package org.obd.metrics.api;

import java.net.URL;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Builder
public class Pids {

	public static final Pids DEFAULT = Pids
	        .builder()
	        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
	        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
	        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
	@Getter
	@NonNull
	@Singular("resource")
	private List<URL> resources;
}
