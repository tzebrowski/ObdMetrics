package org.obd.metrics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EventsPublishlisher<R extends Reply<?>> implements Observer<R> {

	private static final class Reflections {
		@SuppressWarnings("serial")
		private final Map<String, String> fallback = new HashMap<String, String>() {
			{
				put("org.obd.metrics.DataCollector", "org.obd.metrics.Reply");
				put("org.obd.metrics.diagnostic.DefaultDiagnostics", "org.obd.metrics.ObdMetric");
				put("org.openobd2.core.logger.bl.datalogger.MetricsAggregator", "org.obd.metrics.Reply");
			}
		};

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

	private final Map<String, PublishSubject<R>> publishers = new HashMap<>();
	private final Reflections reflections = new Reflections();

	@Builder
	static EventsPublishlisher<Reply<?>> build(@Singular("observer") List<ReplyObserver<Reply<?>>> observers) {
		final EventsPublishlisher<Reply<?>> instance = new EventsPublishlisher<>();
		observers.forEach(instance::subscribe);
		return instance;
	}

	@Override
	public void onCompleted() {
		publishers.values().forEach((publishSubject) -> publishSubject.onCompleted());
	}

	@Override
	public void onError(Throwable o) {
		publishers.values().forEach((publishSubject) -> publishSubject.onError(o));
	}

	@Override
	public void onNext(R reply) {

		PublishSubject<R> publishSubject = publishers.get(reply.getCommand().getClass().getName());
		if (publishSubject != null) {
			publishSubject.onNext(reply);
		}

		Class<?> clazz = reply.getClass();
		while (clazz != null) {
			publishSubject = publishers.get(clazz.getName());
			if (publishSubject != null) {
				publishSubject.onNext(reply);
			}
			clazz = clazz.getSuperclass();
		}
	}

	private void subscribeFor(ReplyObserver<R> replyObserver, List<String> types) {
		for (final String type : types) {
			log.debug("Subscribing observer: {} for: {}", replyObserver.getClass().getSimpleName(), type);
			findPublishSubjectBy(type).subscribe(replyObserver);
		}
	}

	private void subscribe(ReplyObserver<R> replyObserver) {
		if (replyObserver.subscribeFor().isEmpty()) {
			subscribeFor(replyObserver, Arrays.asList(reflections.getParameterizedType(replyObserver)));
		} else {
			subscribeFor(replyObserver,
			        replyObserver.subscribeFor().stream().map(p -> p.getName()).collect(Collectors.toList()));
		}
	}

	private PublishSubject<R> findPublishSubjectBy(final String type) {
		PublishSubject<R> publishSubject = null;
		if (publishers.containsKey(type)) {
			publishSubject = (PublishSubject<R>) publishers.get(type);
		} else {
			publishSubject = PublishSubject.create();
			publishers.put(type, publishSubject);
		}
		return publishSubject;
	}
}
