package org.obd.metrics.pid;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.codec.AnswerCodeCodec;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements PidDefinitionRegistry {

	private final Map<Long, String> idCache = new HashMap<>();
	private final MultiValuedMap<String, PidDefinition> definitions = new ArrayListValuedHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private String mode;
	private final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(true);

	@Override
	public void register(@NonNull PidDefinition pidDefinition) {
		log.debug("Register new pid: {}", pidDefinition);
		definitions.put(answerCodeCodec.getSuccessAnswerCode(pidDefinition), pidDefinition);
		definitions.put(toId(pidDefinition), pidDefinition);
		definitions.put(toId(pidDefinition.getId()), pidDefinition);
	}

	@Override
	public void register(List<PidDefinition> pids) {
		pids.forEach(this::register);
	}

	@Override
	public PidDefinition findBy(@NonNull Long id) {
		return getFirstOne(toId(id));
	}

	@Override
	public PidDefinition findBy(String pid) {
		return getFirstOne((mode + pid));
	}

	@Override
	public Collection<PidDefinition> findBy(PidType definitionType) {
		return definitions.values().stream().filter(p -> p.getDefinitionType() == definitionType)
				.sorted((a, b) -> a.getMode().compareTo(b.getMode())).collect(Collectors.toSet());
	}

	@Override
	public Collection<PidDefinition> findAllBy(PidDefinition pid) {
		if (pid == null) {
			return Collections.emptyList();
		}

		return definitions.get(toId(pid));
	}

	@Override
	public Collection<PidDefinition> findAll() {
		return findBy(PidType.LIVEDATA);
	}

	void load(final Resource resource) {

		try {
			if (null == resource) {
				log.error("Was not able to load pids configuration");
			} else {
				long tt = System.currentTimeMillis();
				final PidResourceFile pidResourceFile = objectMapper.readValue(resource.getInputStream(),
						PidResourceFile.class);

				loadPIDs(pidResourceFile.getDtc(), resource.getName(), PidType.DTC);
				loadPIDs(pidResourceFile.getLivedata(), resource.getName(), PidType.LIVEDATA);
				loadPIDs(pidResourceFile.getMetadata(), resource.getName(), PidType.METADATA);

				this.mode = pidResourceFile.getLivedata().get(0).getMode();
				tt = System.currentTimeMillis() - tt;
				log.info("Load {} PID definitions from stream. Operation took: {}ms",
						pidResourceFile.getLivedata().size(), tt);
			}
		} catch (IOException e) {
			log.error("Failed to load definition file", e);
		}
	}

	private void loadPIDs(final List<PidDefinition> data, final String resourceFile, final PidType definitionType) {
		for (final PidDefinition pid : data) {
			pid.setResourceFile(resourceFile);
			pid.setDefinitionType(definitionType);
			definitions.put(answerCodeCodec.getSuccessAnswerCode(pid), pid);
			definitions.put(toId(pid), pid);
			definitions.put(toId(pid.getId()), pid);
		}
	}

	private String toId(Long id) {
		return id.toString();
	}

	private PidDefinition getFirstOne(String id) {
		return definitions.get(id).stream().findFirst().orElse(null);
	}

	private String toId(PidDefinition pid) {

		if (idCache.containsKey(pid.getId())) {
			return idCache.get(pid.getId());
		} else {
			final String id = (pid.getMode() + pid.getPid());
			idCache.put(pid.getId(), id);
			return id;
		}
	}
}
