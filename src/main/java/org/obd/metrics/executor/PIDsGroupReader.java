package org.obd.metrics.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class PIDsGroupReader <T> extends ReplyObserver<Reply<?>> {

	private final PIDsGroup group;

	@Getter
	protected T value;
	
	@Override
	public List<Class<?>> subscribeFor() {
		final Set<Class<?>> collect = Context.instance()
			.resolve(PidDefinitionRegistry.class)
			.get()
			.findBy(group)
			.stream()
			.map(p -> {
				try {
					return p.getCommandClass() == null ? null : Class.forName(p.getCommandClass());
				} catch (ClassNotFoundException e) {
					return null;
				}
			})
			.filter(p -> p != null)
			.collect(Collectors.toSet());
		
		collect.add(group.getDefaultCommandClass());
		return new ArrayList<>(collect);
	}
}
