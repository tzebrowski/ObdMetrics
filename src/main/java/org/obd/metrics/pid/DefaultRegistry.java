package org.obd.metrics.pid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

	private final MultiValuedMap<String, PidDefinition> definitions = new ArrayListValuedHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AnswerCodeCodec decoder = new AnswerCodeCodec();
	private String mode;

	@Override
	public void register(@NonNull PidDefinition pidDefinition) {
		log.debug("Register new pid: {}", pidDefinition);
		definitions.put(decoder.getSuccessAnswerCode(pidDefinition), pidDefinition);
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
		return getFirstOne((mode + pid).toLowerCase());
	}

	@Override
	public Collection<PidDefinition> findAllBy(PidDefinition pid) {
		if (pid == null) {
			return Arrays.asList();
		}

		return definitions.get(toId(pid));
	}

	@Override
	public Collection<PidDefinition> findAll() {
		return new HashSet<PidDefinition>(definitions.values());
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load pids configuration");
			} else {
				final PidDefinition[] readValue = objectMapper.readValue(inputStream, PidDefinition[].class);
				log.info("Load {} pid definitions", readValue.length);
				for (final PidDefinition pidDef : readValue) {
					definitions.put(decoder.getSuccessAnswerCode(pidDef), pidDef);
					definitions.put(toId(pidDef), pidDef);
					definitions.put(toId(pidDef.getId()), pidDef);
				}
				this.mode = readValue[0].getMode();
			}
		} catch (IOException e) {
			log.error("Failed to load definitin file", e);
		}
	}

	private String toId(long id) {
		return "pid." + id;
	}

	private PidDefinition getFirstOne(String id) {
		return definitions.get(id).stream().findFirst().orElse(null);
	}

	private String toId(PidDefinition pid) {
		return (pid.getMode() + pid.getPid()).toLowerCase();
	}

}
