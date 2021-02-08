package org.obd.metrics.pid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.codec.MetricsDecoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements PidRegistry {

	private final MultiValuedMap<String, PidDefinition> definitions = new ArrayListValuedHashMap<>();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private MetricsDecoder decoder = new MetricsDecoder();
	private String mode;
	
	@Override
	public void register(@NonNull PidDefinition pidDef) {
		log.debug("Register new pid: {}",pidDef);
		definitions.put(decoder.getPredictedAnswerCode(pidDef), pidDef);
		definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
	}

	@Override
	public void register(Collection<PidDefinition> pids) {
		pids.forEach(this::register);
	}
	
	@Override
	public PidDefinition findByAnswerRawData(String rawData) {
		var answerCode = decoder.getAnswerCode(rawData);
		log.debug("Answer code: {}", answerCode);
		return definitions.get(answerCode).stream().findFirst().orElse(null);
	}

	@Override
	public PidDefinition findBy(String pid) {
		return findBy(mode, pid);
	}
	
	@Override
	public Collection<PidDefinition> findAllBy(String pid) {
		return definitions.get((mode + pid).toLowerCase());
	}

	@Override
	public PidDefinition findBy(@NonNull String mode, @NonNull String pid) {
		//fallback to an behavior - first on the list 
		return definitions.get((mode + pid).toLowerCase()).stream().findFirst().orElse(null);
	}

	public Collection<PidDefinition> getDefinitions() {
		return new HashSet<PidDefinition>(definitions.values());
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load pids configuration");
			} else {
				var readValue = objectMapper.readValue(inputStream, PidDefinition[].class);
				log.info("Load {} pid definitions", readValue.length);
				for (var pidDef : readValue) {
					definitions.put(decoder.getPredictedAnswerCode(pidDef), pidDef);
					definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
				}

				//
				this.mode = readValue[0].getMode();
			}
		} catch (IOException e) {
			log.error("Failed to load definitin file", e);
		}
	}

	
}
