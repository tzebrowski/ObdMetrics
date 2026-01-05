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
package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.context.Service;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventsPublishlisher<R extends Reply<?>> implements Observer<R>, Service {

	private final MultiValuedMap<ReplyObserver<?>,Subscription> subscriptions = new ArrayListValuedHashMap<>();
	private final Map<String, PublishSubject<R>> publishers = new HashMap<>();
	private Reflections reflections;

	@Builder
	static EventsPublishlisher<Reply<?>> build(@Singular("observer") List<ReplyObserver<Reply<?>>> observers) {
		final EventsPublishlisher<Reply<?>> instance = new EventsPublishlisher<>();
		instance.reflections = new Reflections(buildFallbackMap(observers));
		observers.forEach(instance::subscribe);
		return instance;
	}
	
	public void unsubscribe(ReplyObserver<R> replyObserver) {
		final Collection<Subscription> subs = subscriptions.get(replyObserver);
		if (subs != null) {
			subs.forEach(p -> {
				p.unsubscribe();
				log.debug("Unsubscribed={} for={}", p, replyObserver.getClass());
			});
		}
	}

	public void subscribe(ReplyObserver<R> replyObserver) {
		if (replyObserver.subscribeFor().isEmpty()) {
			subscriptions.putAll(replyObserver,
					subscribeFor(replyObserver, Arrays.asList(reflections.getParameterizedType(replyObserver))));
		} else {
			subscriptions.putAll(replyObserver,subscribeFor(replyObserver,
					replyObserver.subscribeFor().stream().map(p -> p.getName()).collect(Collectors.toList())));
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
	
	private List<Subscription> subscribeFor(ReplyObserver<R> replyObserver, List<String> types) {
		final List<Subscription> subscriptions = new ArrayList<>(); 
		 
		for (final String type : types) {
			log.debug("Subscribing observer: {} for: {}", replyObserver.getClass().getSimpleName(), type);
			final Subscription subscribe = findPublishSubjectBy(type).subscribe(replyObserver);
			subscriptions.add(subscribe);
		}
		return subscriptions;
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
