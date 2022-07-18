package org.obd.metrics.pid;

import java.io.InputStream;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class Resource {
	private final InputStream inputStream;
	private final String name;
}