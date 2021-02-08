package org.obd.metrics.pid;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface PidRegistry {

	void register(PidDefinition def);
	
	void register(Collection<PidDefinition> pids);

	PidDefinition findByAnswerRawData(String rawData);

	PidDefinition findBy(String mode, String pid);

	PidDefinition findBy(String pid);
	Collection<PidDefinition> findAllBy(String pid);

	public Collection<PidDefinition> getDefinitions();

	@Builder
	public static PidRegistry build(@NonNull @Singular("source") List<InputStream> sources) {
		var instance = new DefaultRegistry();
		sources.forEach(inputStream -> {
			instance.load(inputStream);
		});
		return instance;
	}
}