 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.translation;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads translations from a classpath JSON resource at {@code translations/{locale}.json}.
 *
 * <p>JSON structure:
 * <pre>{
 *   "pids": { "6": { "description": "...", "longDescription": "..." }, ... },
 *   "dtcSystems": { "P": "...", "C": "...", ... },
 *   "dtcCategories": { "0": "...", "1": "...", ... },
 *   "dtcPowertrain": { "0": "...", "1": "...", ... },
 *   "dtcFailureTypes": { "01": "...", ... },
 *   "dtcStatusBits": { "Test Failed": "...", ... }
 * }</pre>
 */
@Slf4j
public final class JsonFileTranslationProvider implements TranslationProvider {

	private final PidTranslations pidTranslations;
	private final DtcTranslations dtcTranslations;
	private final Map<String, String> unitsMap;

	public JsonFileTranslationProvider(final String locale) {
		final String resourcePath = "translations/" + locale + ".json";
		TranslationFile file = loadFromClasspath(resourcePath);

		if (file == null) {
			log.warn("Translation file not found for locale '{}', falling back to defaults.", locale);
			this.pidTranslations = new PidTranslations();
			this.dtcTranslations = new DtcTranslations();
			this.unitsMap = Collections.emptyMap();
		} else {
			log.info("Loaded translation file for locale '{}'.", locale);
			this.pidTranslations = file.pids != null ? new PidTranslations(file.pids) : new PidTranslations();
			this.dtcTranslations = new DtcTranslations(
					file.dtcSystems != null ? file.dtcSystems : Collections.emptyMap(),
					file.dtcCategories != null ? file.dtcCategories : Collections.emptyMap(),
					file.dtcPowertrain != null ? file.dtcPowertrain : Collections.emptyMap(),
					file.dtcFailureTypes != null ? file.dtcFailureTypes : Collections.emptyMap(),
					file.dtcStatusBits != null ? file.dtcStatusBits : Collections.emptyMap());
			this.unitsMap = file.units != null ? file.units : Collections.emptyMap();
		}
	}

	@Override
	public String translatePidDescription(Long pidId, String defaultDescription) {
		return pidTranslations.getDescription(pidId, defaultDescription);
	}

	@Override
	public String translatePidLongDescription(Long pidId, String defaultLongDescription) {
		return pidTranslations.getLongDescription(pidId, defaultLongDescription);
	}

	@Override
	public String translateDtcSystem(Character key, String defaultValue) {
		return dtcTranslations.systems.getOrDefault(String.valueOf(key), defaultValue);
	}

	@Override
	public String translateDtcCategory(Character key, String defaultValue) {
		return dtcTranslations.categories.getOrDefault(String.valueOf(key), defaultValue);
	}

	@Override
	public String translateDtcPowerTrain(Character key, String defaultValue) {
		return dtcTranslations.powertrain.getOrDefault(String.valueOf(key), defaultValue);
	}

	@Override
	public String translateDtcFailureType(String key, String defaultValue) {
		return dtcTranslations.failureTypes.getOrDefault(key, defaultValue);
	}

	@Override
	public String translateDtcStatusBit(String key, String defaultValue) {
		return dtcTranslations.statusBits.getOrDefault(key, defaultValue);
	}

	@Override
	public String translateUnits(String defaultUnits) {
		return unitsMap.getOrDefault(defaultUnits, defaultUnits);
	}

	private TranslationFile loadFromClasspath(String resourcePath) {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
			if (is == null) {
				is = JsonFileTranslationProvider.class.getClassLoader().getResourceAsStream(resourcePath);
			}
			if (is == null) {
				is = JsonFileTranslationProvider.class.getResourceAsStream("/" + resourcePath);
			}
			if (is == null) {
				log.warn("Translation file not found via any classloader: {}", resourcePath);
				return null;
			}
			return new ObjectMapper().readValue(is, TranslationFile.class);
		} catch (Exception e) {
			log.error("Failed to parse translation file: {}", resourcePath, e);
			return null;
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception ignored) {}
			}
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class TranslationFile {
		public Map<String, PidEntry> pids;
		public Map<String, String> dtcSystems;
		public Map<String, String> dtcCategories;
		public Map<String, String> dtcPowertrain;
		public Map<String, String> dtcFailureTypes;
		public Map<String, String> dtcStatusBits;
		public Map<String, String> units;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class PidEntry {
		@Getter
		public String description;
		@Getter
		public String longDescription;
	}

	private static final class PidTranslations {
		private final Map<String, PidEntry> entries;

		PidTranslations() {
			this.entries = Collections.emptyMap();
		}

		PidTranslations(Map<String, PidEntry> entries) {
			this.entries = entries;
		}

		String getDescription(Long pidId, String defaultValue) {
			PidEntry entry = entries.get(String.valueOf(pidId));
			return (entry != null && entry.description != null) ? entry.description : defaultValue;
		}

		String getLongDescription(Long pidId, String defaultValue) {
			PidEntry entry = entries.get(String.valueOf(pidId));
			return (entry != null && entry.longDescription != null) ? entry.longDescription : defaultValue;
		}
	}

	private static final class DtcTranslations {
		final Map<String, String> systems;
		final Map<String, String> categories;
		final Map<String, String> powertrain;
		final Map<String, String> failureTypes;
		final Map<String, String> statusBits;

		DtcTranslations() {
			this.systems = Collections.emptyMap();
			this.categories = Collections.emptyMap();
			this.powertrain = Collections.emptyMap();
			this.failureTypes = Collections.emptyMap();
			this.statusBits = Collections.emptyMap();
		}

		DtcTranslations(Map<String, String> systems, Map<String, String> categories,
				Map<String, String> powertrain, Map<String, String> failureTypes,
				Map<String, String> statusBits) {
			this.systems = systems;
			this.categories = categories;
			this.powertrain = powertrain;
			this.failureTypes = failureTypes;
			this.statusBits = statusBits;
		}
	}
}
