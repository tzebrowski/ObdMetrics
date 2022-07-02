package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Pids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Resources implements AutoCloseable {

	@Getter
	final List<InputStream> resources;

	public static Resources convert(Pids pids) {
		final List<InputStream> resources = pids.getResources().stream().map(p -> {
			try {
				return p.openStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		return new Resources(resources);
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