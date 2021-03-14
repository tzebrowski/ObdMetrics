package org.obd.metrics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class HierarchicalPublishSubject<R extends Reply<?>> implements Observer<R> {

	static final class Reflections {

		static String getParametrizedType(Object o) {

			final Class<?> clazz = o.getClass();
			Type superclass = clazz.getGenericSuperclass();

			if (superclass instanceof ParameterizedType) {
				return getClassName((ParameterizedType) superclass);
			} else {
				superclass = clazz.getSuperclass().getGenericSuperclass();
				if (superclass instanceof ParameterizedType) {
					return getClassName((ParameterizedType) superclass);
				}
			}

			return null;
		}

		private static String getClassName(ParameterizedType mySuperclass) {
			final Type type = mySuperclass.getActualTypeArguments()[0];
			final String typeName = type.getTypeName();
			final int indexOf = typeName.indexOf("<");
			return indexOf > 0 ? typeName.substring(0, indexOf) : typeName;
		}
	}

	private final Map<String, PublishSubject<R>> publishers = new HashMap<>();

	@Builder
	static HierarchicalPublishSubject<Reply<?>> build(@Singular("observer") List<ReplyObserver<Reply<?>>> observers) {
		final HierarchicalPublishSubject<Reply<?>> instance = new HierarchicalPublishSubject<>();

		if (null == observers || observers.isEmpty()) {
			log.info("No subscriber specified.");
		} else {
			observers.forEach(f -> {
				instance.subscribe(f);
			});
		}
		return instance;
	}

	@Override
	public void onCompleted() {
		publishers.forEach((k, publishSubject) -> {
			publishSubject.onCompleted();
		});
	}

	@Override
	public void onError(Throwable o) {
		publishers.forEach((k, publishSubject) -> {
			publishSubject.onError(o);
		});
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

	private void subscribeFor(ReplyObserver<R> replyObserver, String... types) {
		for (final String type : types) {
			log.info("Subscribing observer: {} for: {}", replyObserver.getClass().getSimpleName(), type);
			findPublishSubjectBy(type).subscribe(replyObserver);
		}
	}

	private void subscribe(ReplyObserver<R> replyObserver) {
		if (replyObserver.observables().length == 0) {
			subscribeFor(replyObserver, Reflections.getParametrizedType(replyObserver));
		} else {
			subscribeFor(replyObserver, replyObserver.observables());
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
