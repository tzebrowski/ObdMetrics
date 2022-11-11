package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.Urls;

public interface SimpleWorkflowFactory {

	static Workflow getWorkflow(final Lifecycle lifecycle) throws IOException {
		return getWorkflow(lifecycle, new DataCollector());
	}

	static Workflow getWorkflow() throws IOException {
		return getWorkflow(new SimpleLifecycle(), new DataCollector());
	}

	static Workflow getWorkflow(final DataCollector dataCollector) throws IOException {
		return getWorkflow(new SimpleLifecycle(), dataCollector);
	}

	static Workflow getWorkflow(Lifecycle lifecycle, DataCollector dataCollector) throws IOException {
		return getWorkflow(lifecycle, dataCollector, "mode01.json", "alfa.json", "extra.json");
	}

	static <T extends ReplyObserver> Workflow getWorkflow(Lifecycle lifecycle, T dataCollector, String... pidFiles)
	        throws IOException {

		Pids.PidsBuilder pids = Pids
		        .builder();

		for (final String pidFile : pidFiles) {
			pids = pids.resource(Urls.resourceToUrl(pidFile));
		}
		return Workflow
				.instance()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build())
		        .lifecycle(lifecycle)
		        .pids(pids.build())
		        .observer(dataCollector)
		        .initialize();
	}
}
