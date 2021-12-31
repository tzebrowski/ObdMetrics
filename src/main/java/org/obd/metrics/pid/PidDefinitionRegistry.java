package org.obd.metrics.pid;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface PidDefinitionRegistry {

	void register(PidDefinition def);

	void register(List<PidDefinition> pids);

	PidDefinition findBy(Long id);

	PidDefinition findBy(String pid);

	Collection<PidDefinition> findAllBy(PidDefinition pid);

	Collection<PidDefinition> findAll();

	@Builder
	static PidDefinitionRegistry build(@NonNull @Singular("source") List<InputStream> sources) {
		final DefaultRegistry instance = new DefaultRegistry();
		sources.forEach(instance::load);
		return instance;
	}
}