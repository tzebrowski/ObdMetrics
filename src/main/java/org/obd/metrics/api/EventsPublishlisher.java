/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.context.Service;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventsPublishlisher<R extends Reply<?>> implements Observer<R>, Service {

	
	@RequiredArgsConstructor
	private static final class Reflections {
		
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

	private final Map<String, PublishSubject<R>> publishers = new HashMap<>();
	private Reflections reflections;

	@Builder
	static EventsPublishlisher<Reply<?>> build(@Singular("observer") List<ReplyObserver<Reply<?>>> observers) {
		final EventsPublishlisher<Reply<?>> instance = new EventsPublishlisher<>();
		instance.reflections = new Reflections(buildFallbackMap(observers));
		observers.forEach(instance::subscribe);
		return instance;
	}

	

	public void subscribe(ReplyObserver<R> replyObserver) {
		if (replyObserver.subscribeFor().isEmpty()) {
			subscribeFor(replyObserver, Arrays.asList(reflections.getParameterizedType(replyObserver)));
		} else {
			subscribeFor(replyObserver,
					replyObserver.subscribeFor().stream().map(p -> p.getName()).collect(Collectors.toList()));
		}
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
	
	private static Map<String, String> buildFallbackMap(List<ReplyObserver<Reply<?>>> observers) {
		@SuppressWarnings("serial")
		final Map<String, String> fallback = new HashMap<String, String>() {
			{
				put("org.obd.metrics.diagnostic.DefaultDiagnostics", ObdMetric.class.getName());
			}
		};
		
		observers.forEach(o -> 
		{		
			if (!fallback.containsKey(o.getClass().getName())) {
				fallback.put(o.getClass().getName(), Reply.class.getName());
			}
		});
		
		log.info("Created fallback map: {} for event publisher", fallback);
		return fallback;
	}
}
