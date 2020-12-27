package org.openobd2.core.pid;

import java.io.InputStream;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface PidDefinitionRegistry {

	PidDefinition findByAnswerRawData(String rawData);

	PidDefinition findBy(String mode, String pid);

	@Builder
	public static PidDefinitionRegistry build(@NonNull @Singular("source") List<InputStream> sources) {

		final Registry instance = new Registry();
		sources.forEach(inputStream -> {
			instance.load(inputStream);
		});
		return instance;
	}
}