package org.openobd2.core;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ProducerPolicy {

	@Getter
	private long frequency;

	@Getter
	private List<String> pids;
} 
