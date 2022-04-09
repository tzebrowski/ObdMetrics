package org.obd.metrics.codec.formula;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;

import org.obd.metrics.api.CacheConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CachePersitence {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	Map<Long, Number> load(CacheConfig cacheConfig) {
		synchronized (objectMapper) {
			try (final FileInputStream fis = new FileInputStream(cacheConfig.getResultCacheFilePath())) {
				final Map<Long, Number> items = (Map<Long, Number>) objectMapper.readValue(fis, Map.class);
				log.info("Load cache file from the disk: {}. Found {} entries", cacheConfig.getResultCacheFilePath(),
				        items.size());
				return items;
			} catch (Exception e) {
				log.trace("Failed to load cache from the disk", e);
				log.warn("Failed to load cache from the disk: {}", e.getMessage());
			}
			return Collections.emptyMap();
		}
	}

	void store(CacheConfig cacheConfig, Map<Long, Number> items) {
		synchronized (objectMapper) {
			try (FileOutputStream fos = new FileOutputStream(cacheConfig.getResultCacheFilePath())) {
				log.info("Store cache file from the disk: {}. Number of entries: {} ",
				        cacheConfig.getResultCacheFilePath(),
				        items.size());

				objectMapper.writeValue(fos, items);
			} catch (Exception e) {
				log.trace("Failed to store cache on the disk", e);
				log.warn("Failed to store cache on the disk: {}", e.getMessage());
			}
		}
	}
}
