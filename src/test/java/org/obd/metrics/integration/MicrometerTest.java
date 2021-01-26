package org.obd.metrics.integration;

import org.junit.jupiter.api.Test;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

public class MicrometerTest {
	
	@Test
    public void distributionSummaryTest() {
		final MetricRegistry metrics = new MetricRegistry();

		Histogram histogram = metrics.histogram("aaa");
		
        histogram.update(-40);
        histogram.update(2);
        histogram.update(4);
        histogram.update(5);
       
        Histogram histogram2 = metrics.histogram("aaa");
        histogram2.update(6);
        histogram2.update(50);
        	
        System.out.println(histogram2.getCount());
        
        
        
    }
	
}
