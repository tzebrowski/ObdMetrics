package org.obd.metrics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class HierarchicalPublisher<T extends Reply<?>> implements Observer<T> {

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
			String typeName = type.getTypeName();
			final int indexOf = typeName.indexOf("<");
			if (indexOf > 0) {
				typeName = typeName.substring(0, indexOf);
			}
			return typeName;
		}
	}

	private final Map<String, PublishSubject<T>> publishers = new HashMap<>();

	void subscribe(ReplyObserver<T> replyObserver) {
		subscribeFor(replyObserver, Reflections.getParametrizedType(replyObserver));
	}

	void subscribeFor(ReplyObserver<T> replyObserver, String... types) {
		for (final String type : types) {
			log.info("Subscribing observer: {} for: {}", replyObserver.getClass().getSimpleName(), type);
			getPublishSubject(type).subscribe(replyObserver);
		}
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
	public void onNext(T o) {

		Class<?> clazz = o.getClass();
		while (clazz != null) {
			final PublishSubject<T> publishSubject = publishers.get(clazz.getName());
			if (publishSubject != null) {
				publishSubject.onNext(o);
			}
			clazz = clazz.getSuperclass();
		}
	}

	private PublishSubject<T> getPublishSubject(final String type) {
		PublishSubject<T> publishSubject = null;
		if (publishers.containsKey(type)) {
			publishSubject = (PublishSubject<T>) publishers.get(type);
		} else {
			publishSubject = PublishSubject.create();
			publishers.put(type, publishSubject);
		}
		return publishSubject;
	}
}
