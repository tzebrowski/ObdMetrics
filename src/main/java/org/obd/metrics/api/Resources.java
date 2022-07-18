package org.obd.metrics.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Pids;
import org.obd.metrics.pid.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Resources implements AutoCloseable {

	@Getter
	final List<Resource> resources;

	public static Resources convert(Pids pids) {
		final List<Resource> resources = pids.getResources().stream().map(p -> {
			try {
				return Resource.builder().inputStream(p.openStream()).name(new File(p.getFile()).getName()).build();
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
				f.getInputStream().close();
			} catch (IOException e) {
			}
		});
	}
}