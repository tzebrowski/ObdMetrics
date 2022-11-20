package org.obd.metrics;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public interface PIDsRegistry extends PidDefinitionRegistry {

	default PidDefinition findBy(String pid) {
		return findAll()
				.stream()
				.filter(p -> p.getPid().equals(pid))
				.findFirst()
				.orElse(null);
	}
}