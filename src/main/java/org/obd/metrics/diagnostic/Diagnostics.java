package org.obd.metrics.diagnostic;

/**
 * Facade interface that provide diagnosis information about current state of
 * data collection.
 * 
 * @author tomasz.zebrowski
 */
public interface Diagnostics {

	HistogramSupplier histogram();

	RateSupplier rate();
	
	void reset();

	static Diagnostics instance() {
		return new DefaultDiagnostics();
	}
}
