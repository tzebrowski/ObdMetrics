package org.obd.metrics.pid;

import java.util.Collection;
import java.util.List;

import org.obd.metrics.context.Service;

import lombok.Builder;
import lombok.Singular;

public interface PidDefinitionRegistry extends Service {

	void register(PidDefinition def);

	void register(List<PidDefinition> pids);

	PidDefinition findBy(Long id);
	
	Collection<PidDefinition> findAllBy(PidDefinition pid);

	Collection<PidDefinition> findAll();
	
	Collection<PidDefinition> findBy(PIDsGroup group);
	
	@Builder
	static PidDefinitionRegistry build(@Singular("source") List<Resource> sources) {
		final DefaultPIDsRegistry instance = new DefaultPIDsRegistry();
		sources.forEach(instance::load);
		return instance;
	}
}