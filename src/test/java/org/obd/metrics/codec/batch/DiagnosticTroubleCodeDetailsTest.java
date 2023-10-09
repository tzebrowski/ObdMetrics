/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.decoder.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

@Disabled
public class DiagnosticTroubleCodeDetailsTest {

	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy(7001l)));
		commands.add(new ObdCommand(registry.findBy(7002l)));
		commands.add(new ObdCommand(registry.findBy(7003l)));
		
		//1904 26E400
		final byte[] message = "08C0:590426E400481:002110080000BF2:4F10090000200A3:0C0660820019214:100001000000005:080010000000196:24001802009A107:020000192D00208:010704501812009:00100338100400A:77193703E91923B:02191E0120182FC:0000193537195AD:03EC1821001818E:00195603F81302F:00111B0D0000000:000000000019BD1:481B03C1198C012:19BE00200000193:280010016B37014:00".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

	
		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7001l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7002l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7003l)), batchMessage);
	}
}
