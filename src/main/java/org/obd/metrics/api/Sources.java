package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Sources implements AutoCloseable {

	@Getter
	final List<InputStream> resources;

	public static Sources open(PidSpec pidSpec) {
		final List<InputStream> resources = pidSpec.getSources().stream().map(p -> {
			try {
				return p.openStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		return new Sources(resources);
	}

	@Override
	public void close() {

		resources.forEach(f -> {
			try {
				f.close();
			} catch (IOException e) {
			}
		});

	}

}