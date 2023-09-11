package org.obd.metrics.alert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultAlertManager extends ReplyObserver<ObdMetric> implements Alerts {

	private Map<PidDefinition, List<Alert>> allerts = new HashMap<>();

	@Override
	public void onNext(ObdMetric t) {
		if (t.isAlert()) {
			log.error("In alert={}", t);
			append(t);
		}
	}

	@Override
	public void reset() {
		allerts.clear();
	}

	@Override
	public List<Alert> findBy(PidDefinition pid) {
		if (allerts.containsKey(pid)) {
			return allerts.get(pid);
		} else {
			return List.of();
		}
	}
	
	@Override
	public Map<PidDefinition, List<Alert>> findAll(){
		return allerts;
	}
	
	private void append(ObdMetric t) {
		final Alert a = new Alert(t.getValue(), t.getTimestamp());
		if (allerts.containsKey(t.getCommand().getPid())) {
			allerts.get(t.getCommand().getPid()).add(a);
		} else {
			allerts.put(t.getCommand().getPid(), Arrays.asList(a));
		}
	}
}
