package org.obd.metrics.pid;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultPIDsRegistry implements PidDefinitionRegistry {

	private final MultiValuedMap<String, PidDefinition> definitions = new ArrayListValuedHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void register(@NonNull PidDefinition pidDefinition) {
		log.info("Register new pid: {}", pidDefinition);
		register(null, PIDsGroup.LIVEDATA, pidDefinition);
	}

	@Override
	public void register(List<PidDefinition> pids) {
		pids.forEach(this::register);
	}

	@Override
	public PidDefinition findBy(@NonNull Long id) {
		return getFirstOne(id.toString());
	}

	@Override
	public Collection<PidDefinition> findBy(PIDsGroup group) {
		return definitions.values().stream().filter(p -> p.getGroup() == group)
				.sorted((a, b) -> a.getMode().compareTo(b.getMode())).collect(Collectors.toSet());
	}

	@Override
	public Collection<PidDefinition> findAllBy(PidDefinition pid) {
		if (pid == null) {
			return Collections.emptyList();
		}

		return definitions.get(pid.getQuery());
	}

	@Override
	public Collection<PidDefinition> findAll() {
		return findBy(PIDsGroup.LIVEDATA);
	}

	void load(final Resource resource) {

		try {
			if (null == resource) {
				log.error("Was not able to load pids configuration");
			} else {
				long tt = System.currentTimeMillis();
				final PIDsGroupFile groupFile = objectMapper.readValue(resource.getInputStream(), PIDsGroupFile.class);

				registerPIDsGroup(groupFile.getDtc(), resource.getName(), PIDsGroup.DTC);
				registerPIDsGroup(groupFile.getLivedata(), resource.getName(), PIDsGroup.LIVEDATA);
				registerPIDsGroup(groupFile.getMetadata(), resource.getName(), PIDsGroup.METADATA);
				registerPIDsGroup(groupFile.getCapabilities(), resource.getName(), PIDsGroup.CAPABILITES);

				tt = System.currentTimeMillis() - tt;
				log.info("Load {} PID definitions from stream. Operation took: {}ms", groupFile.getLivedata().size(),
						tt);
			}
		} catch (IOException e) {
			log.error("Failed to load definition file", e);
		}
	}

	private void registerPIDsGroup(final List<PidDefinition> data, final String resourceFile, final PIDsGroup group) {
		data.forEach(pid -> {
			register(resourceFile, group, pid);
		});
	}

	private void register(final String resourceFile, final PIDsGroup group, PidDefinition pid) {
		pid.setResourceFile(resourceFile);
		pid.setGroup(group);
		definitions.put(pid.getQuery(), pid);
		definitions.put(pid.getIdString(), pid);
	}

	private PidDefinition getFirstOne(String id) {
		return definitions.get(id).stream().findFirst().orElse(null);
	}
}
