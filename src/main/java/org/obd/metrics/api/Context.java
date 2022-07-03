package org.obd.metrics.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {

	public static interface Service {
	}

	private static final Context instance = new Context();

	private final Map<Class<? extends Service>, Object> data = new HashMap<>();

	public <T extends Service> Optional<T> lookup(Class<T> clazz) {
		return Optional.ofNullable((T) data.get(clazz));
	}

	public <T extends Service> void register(Class<T> clazz, T t) {
		data.put(clazz, t);
	}

	public static Context instance() {
		return instance;
	}
}
