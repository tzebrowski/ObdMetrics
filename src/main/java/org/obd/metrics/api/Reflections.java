package org.obd.metrics.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class Reflections {
	
	private final Map<String, String> fallback;

	String getParameterizedType(Object o) {

		Class<?> clazz = o.getClass();
		log.debug("Getting parametrizedType for: {}", clazz.getName());

		while (clazz != null) {
			final Type genericSuperclass = clazz.getGenericSuperclass();
			if (genericSuperclass instanceof ParameterizedType) {
				String className = getClassName((ParameterizedType) genericSuperclass);
				if (null == className) {
					className = fallback.get(o.getClass().getName());
				}

				log.debug("Found parametrizedType: {} for: {}", className, clazz.getName());
				return className;
			}
			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private String getClassName(ParameterizedType superClass) {
		try {
			final String typeName = (superClass.getActualTypeArguments()[0]).getTypeName();
			final int indexOf = typeName.indexOf("<");
			return indexOf > 0 ? typeName.substring(0, indexOf) : typeName;

		} catch (Throwable e) {
			log.debug("Error occurred during fetching class name. ", e);
			return null;
		}
	}
}