package org.obd.metrics.pid;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.obd.metrics.api.Context.Service;

import lombok.Builder;
import lombok.Singular;

public interface PidDefinitionRegistry extends Service {

	void register(PidDefinition def);

	void register(List<PidDefinition> pids);

	PidDefinition findBy(Long id);

	PidDefinition findBy(String pid);

	Collection<PidDefinition> findAllBy(PidDefinition pid);

	Collection<PidDefinition> findAll();

	@Builder
	static PidDefinitionRegistry build(@Singular("source") List<InputStream> sources) {
		final DefaultRegistry instance = new DefaultRegistry();
		sources.forEach(instance::load);
		return instance;
	}
}