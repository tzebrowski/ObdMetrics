package org.obd.metrics.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Pids;
import org.obd.metrics.pid.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class Resources implements AutoCloseable {

	@Getter
	final List<Resource> resources;

	public static Resources convert(Pids pids) {
		final List<Resource> resources = pids.getResources().stream()
			.filter(p -> p != null)
			.map(p -> {
				try {
					final File file = new File(p.getFile());
					log.info("Loading resource file: {}. Files exists={}", file, file.exists());
					return Resource.builder().inputStream(p.openStream()).name(file.getName()).build();
	
				} catch (Throwable e) {
					log.warn("Failed to load resource file: {}", p.getFile(), e);
				}
				return null;
			}).filter(p -> p != null)
			.collect(Collectors.toList());
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