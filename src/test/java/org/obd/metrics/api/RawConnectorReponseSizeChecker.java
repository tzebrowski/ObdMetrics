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
package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RawConnectorReponseSizeChecker extends ReplyObserver<ObdMetric> {
	long callCount;
	final int expectedSize;
	
	@Override
	public void onNext(ObdMetric t) {
		Assertions.assertThat(t.getRaw().capacity()).isEqualTo(expectedSize);
		callCount++;
	}
}