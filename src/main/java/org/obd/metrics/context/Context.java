package org.obd.metrics.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {

	private static final Context instance = new Context();

	private final Map<Class<? extends Service>, Object> data = new HashMap<>();

	public <T extends Service> Bean<T> resolve(Class<T> clazz) {
		return Bean.of((T) data.get(clazz));
	}

	public <T extends Service> Bean<T> register(Class<T> clazz, T t) {
		data.put(clazz, t);
		return Bean.of(t);
	}

	public static Context instance() {
		return instance;
	}

	public static void apply(Consumer<Context> action) {
		action.accept(instance);
	}
}
