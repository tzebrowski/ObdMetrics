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
package org.obd.metrics.api.integration.sniffing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.integration.raw.BleRawIntegrationRunner;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SnifferMetric;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.api.model.SniffingPolicy.STNxxExtensions;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.Rate;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sniffer extends BleRawIntegrationRunner {

	private static final class SavvyCANLogOutput extends ReplyObserver<SnifferMetric>{
		private final FileWriter fw;
		private final String header = "Time Stamp,ID,Extended,Dir,Bus,LEN,D1,D2,D3,D4,D5,D6,D7,D8";
		
		SavvyCANLogOutput(String fileName) throws IOException {
			fw = new FileWriter(fileName, false);
			fw.write(header);
		}
		
		@Override
		public void onNext(SnifferMetric t) {
			final long timestamp = System.currentTimeMillis();
			for (final String line : t.getRaw().getMessage().split("\r")) {
				final String segments[] = line.split(" ");
				if (segments.length == 9) {
					final String out = timestamp + "," 
								+ segments[0] + ","
								+ "1, Rx, 0, 8, "
								+ segments[1] + ", "
								+ segments[2] + ", "
								+ segments[3] + ", "
								+ segments[4] + ", "
								+ segments[5] + ", "
								+ segments[6] + ", "
								+ segments[7] + ", "
								+ segments[8] + "\n";
					try {
						fw.write(out);
						fw.flush();
					} catch (IOException e) {
						log.error("Failed to write", e);
					}
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource(value = { 
			"000D18000001,c://work/giulia_out.%d.csv,10000,true,false",
			"AABBCC112233,c://work/vw_out.%d.csv,10000,false,true",
	}, delimiter = ',')
	public void run(final String adapterName,final String fileName,long duration, boolean stnEnabled, boolean enabled) 
			throws IOException, InterruptedException, ExecutionException {
		if (enabled) {
			final AdapterConnection connection = BluetoothConnection.openConnection(adapterName);
	
			final SavvyCANLogOutput output = new SavvyCANLogOutput(String.format(fileName, System.currentTimeMillis()));
			
			final Workflow workflow = Workflow.instance().observer((ReplyObserver) output).initialize();
	
			final SniffingPolicy sniffingPolicy = SniffingPolicy
					.builder()
					.enabled(true)
					.debugEnabled(false)
					.stNxx(STNxxExtensions.builder().enabled(stnEnabled).build()).build();
	
			workflow.start(connection, sniffingPolicy);
			WorkflowFinalizer.finalizeAfter(workflow, duration);
	
			final Optional<Rate> rate = workflow.getDiagnostics().rate().findBy(RateType.MEAN,
					workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));

			Assertions.assertThat(rate.get().getValue()).isGreaterThanOrEqualTo(0);
		
		} else {
			log.warn("Test for adapter={} is disabled.", adapterName);
		}
	}
}
