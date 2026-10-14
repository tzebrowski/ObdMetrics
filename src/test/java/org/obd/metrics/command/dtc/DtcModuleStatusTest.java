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
package org.obd.metrics.command.dtc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DtcModuleStatusTest {

	@Test
	public void classifiesTheRawReadReply() {
		Assertions.assertThat(DtcModuleStatus.of("5902FF")).isEqualTo(DtcModuleStatus.RESPONDED);
		Assertions.assertThat(DtcModuleStatus.of("00B0:5902CF0191131:8F06")).isEqualTo(DtcModuleStatus.RESPONDED);
		Assertions.assertThat(DtcModuleStatus.of("7F1911")).isEqualTo(DtcModuleStatus.REFUSED);
		Assertions.assertThat(DtcModuleStatus.of("7F 19 22")).isEqualTo(DtcModuleStatus.REFUSED);
		Assertions.assertThat(DtcModuleStatus.of("0060:7F1931")).isEqualTo(DtcModuleStatus.REFUSED);
		// Response pending (NRC 78) ahead of the real answer is not a refusal.
		Assertions.assertThat(DtcModuleStatus.of("7F19785902FF")).isEqualTo(DtcModuleStatus.RESPONDED);
		Assertions.assertThat(DtcModuleStatus.of("7F19787F1911")).isEqualTo(DtcModuleStatus.REFUSED);
		Assertions.assertThat(DtcModuleStatus.of(null)).isEqualTo(DtcModuleStatus.NO_RESPONSE);
		Assertions.assertThat(DtcModuleStatus.of(" ")).isEqualTo(DtcModuleStatus.NO_RESPONSE);
	}
}
