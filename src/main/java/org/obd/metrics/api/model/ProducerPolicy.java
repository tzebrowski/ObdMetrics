package org.obd.metrics.api.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@ToString
@Builder
public class ProducerPolicy {

	@SuppressWarnings("serial")
	public static final Map<Integer, Integer> DEFAULT_PID_PRIORITY= new HashMap<Integer, Integer>() {
		{
			put(0, 0);
			put(1, 5);
			put(2, 15);
			put(3, 35);
			put(4, 50);
			put(5, 100);
			put(6, 200);
			put(7, 500);
			put(8, 1000);
			put(9, 5000);
			put(10, 10000);
		}
	};
	
	@SuppressWarnings("serial")
	public static final ProducerPolicy DEFAULT = ProducerPolicy
	        .builder()
	        .pidPriorities(new HashMap<Integer, Integer>() {
				{
					put(0, 0);
					put(1, 5);
					put(2, 20);
					put(3, 50);
					put(4, 100);
					put(5, 200);
					put(6, 500);
					put(7, 1000);
					put(8, 2000);
					put(9, 5000);
					put(10, 10000);
				}
			})
	        .priorityQueueEnabled(Boolean.TRUE)
	        .build();

	@Getter
	@Default
	private boolean priorityQueueEnabled = Boolean.TRUE;
	
	
	@Getter
	@Singular("pidPriority")
	private Map<Integer, Integer> pidPriorities;
}
