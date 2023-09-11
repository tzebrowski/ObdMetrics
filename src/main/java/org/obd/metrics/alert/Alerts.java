package org.obd.metrics.alert;

import java.util.List;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;

public interface Alerts {

	void reset();

	List<Alert> findBy(PidDefinition pid);

	Map<PidDefinition, List<Alert>> findAll();

	static Alerts instance() {
		return new DefaultAlertManager();
	}

}
