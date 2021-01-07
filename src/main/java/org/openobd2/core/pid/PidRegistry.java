package org.openobd2.core.pid;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface PidRegistry {

	PidDefinition findByAnswerRawData(String rawData);

	PidDefinition findBy(String mode, String pid);

	public Collection<PidDefinition> getDefinitions();

	@Builder
	public static PidRegistry build(@NonNull @Singular("source") List<InputStream> sources) {

		final DefaultRegistry instance = new DefaultRegistry();
		sources.forEach(inputStream -> {
			instance.load(inputStream);
		});
		return instance;
	}

}