package org.obd.metrics.codec.formula;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.obd.metrics.api.CacheConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CachePersitence {

	@SuppressWarnings("unchecked")
	Map<Long, Number> load(CacheConfig cacheConfig) {
		try (final FileInputStream fis = new FileInputStream(cacheConfig.getResultCacheFilePath());
		        final ObjectInputStream ois = new ObjectInputStream(fis);) {

			final Map<Long, Number> items = (Map<Long, Number>) ois.readObject();
			log.info("Load cache file from the disk: {}. Found {} entries", cacheConfig.getResultCacheFilePath(),
			        items.size());
			return items;
		} catch (Exception e) {
			log.trace("Failed to load cache from the disk", e);
			log.warn("Failed to load cache from the disk: {}", e.getMessage());
		}
		return Collections.emptyMap();
	}

	void store(CacheConfig cacheConfig, Map<Long, Number> items) {
		try (final ObjectOutputStream oos = new ObjectOutputStream(
		        new FileOutputStream(cacheConfig.getResultCacheFilePath()))) {
			log.info("Store cache file from the disk: {}. Number of entries: {} ", cacheConfig.getResultCacheFilePath(),
			        items.size());

			oos.writeObject(items);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			log.trace("Failed to store cache on the disk", e);
			log.warn("Failed to store cache on the disk: {}", e.getMessage());
		}
	}
}
