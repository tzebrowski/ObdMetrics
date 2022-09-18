package org.obd.metrics.pid;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PIDsGroupFile {
	private List<PidDefinition> dtc = new ArrayList<>();
	private List<PidDefinition> livedata = new ArrayList<>();
	private List<PidDefinition> metadata = new ArrayList<>();
	private List<PidDefinition> capabilities = new ArrayList<>();
}
